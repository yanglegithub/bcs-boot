package com.phy.bcs.service.ifs.netty.client.handler;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.file.service.InfFileStatusService;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.controller.model.*;
import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import com.phy.bcs.service.ifs.netty.server.handler.FepOverTimeHandler;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class FepTcpDbClientHander extends FepOverTimeHandler<ParseFEP> {
    //记录FEP协议进行到哪一步，0:发送FEP请求包; 1:接收请求回应包; 2:发送数据包; 3:接收结束包
    private int step = 0;

    private int fileIndex = 0;

    private int id;
    private List<InfFileStatus> filelist;

    private InfFileStatusService service;

    public FepTcpDbClientHander(List<InfFileStatus> filepaths){
        service = SpringContextHolder.getBean(InfFileStatusService.class);
        filelist = filepaths;
    }
    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, ParseFEP msg) {
        if(step == 1){
            if(!"2".equals(msg.getFlag()))
                return;
            AnswerFEPMode mode = msg.getAnswerFEPMode();
            InfFileStatus filesta = filelist.get(fileIndex);

            if(!filesta.getFileName().contains(mode.getFileName().trim()))
                return;

            if(mode.getNum() < 0){
                closefep(channelHandlerContext);
                if(mode.getNum() == -1) {
                    filesta.setSendFinish(1);
                    service.saveOrUpdate(filesta);
                }
            }else {
                //当前需要传输的文件id
                id = mode.getID();
                step = 2;
                try {
                    send(channelHandlerContext, id, mode.getNum());
                    step = 3;
                } catch (Exception e) {
                    System.out.println("文件读取失败");
                    e.printStackTrace();
                }
            }
        } else if(step == 3){
            if(!"3".equals(msg.getFlag()))
                return;
            FinishFEPMode mode = msg.getFinishFEPMode();
            if(mode.getID() != id)
                return;
            //操作数据库以保存此文件传输完毕的状态
            InfFileStatus filesta = filelist.get(fileIndex);
            filesta.setSendFinish(1);
            filesta.setUpdateTime(new Date());
            service.saveOrUpdate(filesta);

            //传输下一个文件
            step= 0;
            closefep(channelHandlerContext);
            if(fileIndex >= filelist.size()){
                return;
            }
            requestNewFile(channelHandlerContext);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        //当tcp连接建立时，建立fep连接发送请求包，发送第一个文件
        if(fileIndex >= filelist.size())
            return;
        requestNewFile(ctx);
    }

    @Override
    protected void handleAllIdle(ChannelHandlerContext ctx){
        System.out.println("读或写超时，断开tcp连接");
        //对方tcp中断或fep连接中断 关闭tcp连接
        ctx.close();
    }

    private void send(ChannelHandlerContext ctx, Integer id, int num) throws IOException, SQLException {
        int packgesize = SpringContextHolder.getBean(BcsApplicationConfig.class).getPackgesize();
        int off = num;
        InfFileStatus filesta = filelist.get(fileIndex);
        int inlength = filesta.getFileContent().length;
        while (off < inlength){
            boolean sendZero = false;
            byte[] bytes;
            if(inlength-off > packgesize){
                bytes = ParseUtil.getBytes(filesta.getFileContent(), off, packgesize);
            }
            else if(inlength-off == packgesize){
                sendZero = true;
                bytes = ParseUtil.getBytes(filesta.getFileContent(), off, inlength-off);
            }else{
                bytes = ParseUtil.getBytes(filesta.getFileContent(), off, inlength-off);
            }

            DataFEPMode data = new DataFEPMode();
            data.setNum(off);
            data.setID(id);
            data.setData(new String(bytes, "UTF-8"));

            ParseFEP fep = new ParseFEP();
            fep.setFlag("4");
            fep.setDataFEPMode(data);
            off += bytes.length;
            ctx.writeAndFlush(fep);

            //当传输的文件大小为定长字节的整数倍时，传输完毕后要补发一个data长度为0字节的数据包
            if(sendZero){
                DataFEPMode dataZ = new DataFEPMode();
                dataZ.setNum(off);
                dataZ.setID(id);
                dataZ.setData(new String(new byte[0], "UTF-8"));

                ParseFEP fepZ = new ParseFEP();
                fepZ.setFlag("4");
                fepZ.setDataFEPMode(dataZ);
                ctx.writeAndFlush(fepZ);
            }
        }
    }

    private void closefep(ChannelHandlerContext ctx){
        step = 0;
        id = 0;
        fileIndex++;
        if(fileIndex >= filelist.size()){
            ctx.close();
        }
    }

    private void requestNewFile(ChannelHandlerContext ctx) {
        InfFileStatus filestatus = filelist.get(fileIndex);

        SendFEPMode sendfep = new SendFEPMode();
        sendfep.setFileName(filestatus.getFileName());
        sendfep.setFileLength(filestatus.getLength());

        ParseFEP fep = new ParseFEP();
        fep.setFlag("1");
        fep.setSendFEPMode(sendfep);
        ctx.writeAndFlush(fep);
        step = 1;
    }
}

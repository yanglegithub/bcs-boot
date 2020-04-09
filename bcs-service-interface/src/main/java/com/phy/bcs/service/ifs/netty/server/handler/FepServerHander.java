package com.phy.bcs.service.ifs.netty.server.handler;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.file.service.InfFileStatusService;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.controller.model.*;
import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.Date;

public abstract class FepServerHander extends FepOverTimeHandler<ParseFEP> {
    //记录FEP协议进行到哪一步，0:接收FEP请求包; 1:发送请求回应包; 2:接收数据包; 3:发送结束包
    private int step = 0;
    //当前接收的文件信息
    private int id = 0;
    private InfFileStatus filestatus;
    //当前文档大小
    private int filelength = 0;

    private InfFileStatusService service;
    private BcsApplicationConfig config;

    public FepServerHander(){
        super();
        service = SpringContextHolder.getBean(InfFileStatusService.class);
        config = SpringContextHolder.getBean(BcsApplicationConfig.class);
    }


    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, ParseFEP msg) {
        if(step == 0){
            if(!"1".equals(msg.getFlag()))
                return;
            SendFEPMode mode = msg.getSendFEPMode();
            step = 1;
            //生成回应并改状态
            AnswerFEPMode answer = generAnswer(channelHandlerContext, mode);
            ParseFEP res = new ParseFEP();
            res.setFlag("2");
            res.setAnswerFEPMode(answer);
            channelHandlerContext.writeAndFlush(res);
        }else if(step == 2){
            if(!"4".equals(msg.getFlag()))
                return;
            DataFEPMode mode = msg.getDataFEPMode();
            reciveFile(channelHandlerContext, mode);
        }
    }

    @Override
    protected void handleAllIdle(ChannelHandlerContext ctx){
        System.out.println("读或写超时，断开tcp连接");

        //修改保存数据库
        service.saveOrUpdate(filestatus);
        closefep();

        //对方tcp中断或fep连接中断 关闭tcp连接
        ctx.close();
    }

    //接收文件，它需要时会返回结束应答并修改连接状态
    private void reciveFile(ChannelHandlerContext ctx, DataFEPMode mode) {
        int packgesize = config.getPackgesize();
        if(mode.getID() != id || mode.getNum() != filelength)
            return;
        byte[] filebs = mode.getData().getBytes();
        //filebytes = ParseUtil.byteMerger(filebytes, filebs);
        try {
            ParseUtil.setBytes(filestatus.getFileContent(), filelength, filebs);
        } catch (Exception e) {
            e.printStackTrace();
            service.saveOrUpdate(filestatus);
            closefep();
            return;
        }

        filelength += filebs.length;
        if(filebs.length < packgesize){
            FinishFEPMode f = new FinishFEPMode();
            f.setID(id);
            ParseFEP fep = new ParseFEP();
            fep.setFlag("3");
            fep.setFinishFEPMode(f);

            //修改数据库
            filestatus.setRecFinish(1);
            service.saveOrUpdate(filestatus);

            ctx.writeAndFlush(fep);

            closefep();
        }
    }

    //生成应答包，并修改相应的fep连接状态
    private AnswerFEPMode generAnswer(ChannelHandlerContext ctx, SendFEPMode sendMode) {
        //查找数据库，看看是否是有中断续传的文件
        filestatus = service.findOneByFilename(sendMode.getFileName());
        AnswerFEPMode mode = new AnswerFEPMode();
        if(filestatus == null){
            //创建一条filestatus数据
            InfFileStatus newFile = new InfFileStatus();
            newFile.setFileName(sendMode.getFileName().trim());
            newFile.setLength(sendMode.getFileLength());
            newFile.setFileContent(new byte[sendMode.getFileLength()]);
            //从哪个系统接收的
            String ip = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
            newFile.setFromSystem(config.getSyscodeByIp(ip));
            newFile.setFromProto("FEP");
            //到哪个系统去
            newFile.setToSystem(sysTransfor(ctx, sendMode));
            newFile.setToProto(protoTransfor(ctx, sendMode));
            newFile.setRecFinish(0);
            newFile.setSendFinish(0);
            newFile.setTransTimes(0);
            newFile.setCreateTime(new Date());
            service.save(newFile);
            filestatus = newFile;

            //创建AnswerFEPMode
            mode.setID(filestatus.getId());
            mode.setFileName(sendMode.getFileName());
            mode.setNum(0);

            //修改状态
            step = 1;
            id = filestatus.getId();
        }else {
            long length = 0;
            int num = 0;
            try {
                length = filestatus.getFileContent().length;
                num = (int)length;
            }catch(Exception e){
            num = -2;
            }
            if(filestatus.getRecFinish() == 1){
                mode.setID(filestatus.getId());
                mode.setFileName(sendMode.getFileName());
                mode.setNum(-1);
            }else {
                mode.setID(filestatus.getId());
                mode.setFileName(sendMode.getFileName());
                mode.setNum(num);
                //修改状态
                if(num == -2){
                    closefep();
                }else {
                    //修改状态
                    step = 1;
                    id = filestatus.getId();
                    filelength = (int) length;
                }
            }
        }
        return mode;
    }

    //关闭fep连接
    private void closefep(){
        step = 0;
        id = 0;
        filelength = 0;
        filestatus = null;
    }

    /**
     * 文件转发逻辑，应该转发给哪个系统，返回系统的代码
     * @param mode
     * @return
     */
    protected abstract int sysTransfor(ChannelHandlerContext ctx, SendFEPMode mode);

    /**
     * 文件转发逻辑，返回用哪个协议转发，返回协议的字符串，如FEP RECP PDXP
     * @param mode
     * @return
     */
    protected abstract String protoTransfor(ChannelHandlerContext ctx, SendFEPMode mode);
}

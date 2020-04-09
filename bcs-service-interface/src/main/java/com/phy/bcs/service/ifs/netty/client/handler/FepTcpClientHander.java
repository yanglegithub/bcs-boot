package com.phy.bcs.service.ifs.netty.client.handler;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.controller.model.*;
import com.phy.bcs.service.ifs.netty.server.handler.FepOverTimeHandler;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FepTcpClientHander extends FepOverTimeHandler<ParseFEP> {
    //记录FEP协议进行到哪一步，0:发送FEP请求包; 1:接收请求回应包; 2:发送数据包; 3:接收结束包
    private int step = 0;
    //要发送的文件路径
    private String[] filepaths;
    private int fileIndex = 0;
    //private Map<Integer, String> idname = new HashMap<>();
    private int id;
    private String curPath;
    private String curName;

    public FepTcpClientHander(String[] filepaths){
        this.filepaths = filepaths;
    }
    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, ParseFEP msg) {
        if(step == 1){
            if(!"2".equals(msg.getFlag()))
                return;
            AnswerFEPMode mode = msg.getAnswerFEPMode();
            String path = filepaths[fileIndex];

            if(!path.contains(mode.getFileName()))
                return;

            //当前需要传输的文件
            id = mode.getID();
            curPath = path;
            curName = mode.getFileName();

            step = 2;
            try {
                send(channelHandlerContext, mode.getID(), mode.getNum());
                step = 3;
            } catch (IOException e) {
                System.out.println("文件读取失败");
                e.printStackTrace();
            }
        } else if(step == 3){
            if(!"3".equals(msg.getFlag()))
                return;
            FinishFEPMode mode = msg.getFinishFEPMode();
            if(mode.getID() != id)
                return;
            //操作数据库以保存此文件传输完毕的状态


            //传输下一个文件
            step= 0;
            File file;
            do{
                closefep(channelHandlerContext);
                if(fileIndex > filepaths.length)
                    return;
                file = new File(filepaths[fileIndex]);
            }while (!file.exists());
            requestNewFile(channelHandlerContext);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        //当tcp连接建立时，建立fep连接发送请求包，发送第一个文件
        File file = new File(filepaths[fileIndex]);
        while (!file.exists()){
            closefep(ctx);
            if(fileIndex > filepaths.length)
                return;
            file = new File(filepaths[fileIndex]);
        }
        requestNewFile(ctx);
    }

    @Override
    protected void handleAllIdle(ChannelHandlerContext ctx){
        System.out.println("读或写超时，断开tcp连接");
        //对方tcp中断或fep连接中断 关闭tcp连接
        ctx.close();
    }

    private void send(ChannelHandlerContext ctx, Integer id, int num) throws IOException {
        int packgesize = SpringContextHolder.getBean(BcsApplicationConfig.class).getPackgesize();
        int off = num;
        String path = curPath;
        File file = new File(path);
        FileInputStream in = new FileInputStream(file);
        in.skip(num);
        while (in.available() > 0){
            boolean sendZero = false;
            byte[] bytes;
            if(in.available() > packgesize){
                bytes = new byte[packgesize];
                off += packgesize;
            }
            else if(in.available() == packgesize){
                sendZero = true;
                off += in.available();
                bytes = new byte[in.available()];
            }else{
                off += in.available();
                bytes = new byte[in.available()];
            }
            in.read(bytes);

            DataFEPMode data = new DataFEPMode();
            data.setNum(off);
            data.setID(id);
            data.setData(new String(bytes, "UTF-8"));

            ParseFEP fep = new ParseFEP();
            fep.setFlag("4");
            fep.setDataFEPMode(data);
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
        in.close();

    }

    private void closefep(ChannelHandlerContext ctx){
        step = 0;
        id = 0;
        curPath = null;
        curName = null;
        fileIndex++;
        if(fileIndex > filepaths.length)
            ctx.close();
    }

    private void requestNewFile(ChannelHandlerContext ctx) {
        boolean success = false;
        String filepath = filepaths[fileIndex];
        File file = new File(filepath);

        do {
            try{
                FileInputStream in = new FileInputStream(file);
                String filename = file.getName();

                SendFEPMode sendfep = new SendFEPMode();
                sendfep.setFileName(filename);
                sendfep.setFileLength(in.available());
                in.close();

                ParseFEP fep = new ParseFEP();
                fep.setFlag("1");
                fep.setSendFEPMode(sendfep);
                ctx.writeAndFlush(fep);
                step = 1;
                success = true;
            }catch (IOException e){

                //若读取失败 则发送下一个文件
                do{
                    System.out.println("文件:"+filepath+" 读取失败");
                    closefep(ctx);
                    if(fileIndex > filepaths.length)
                        return;
                    file = new File(filepaths[fileIndex]);
                }while (!file.exists());
            }
        }while (!success);

    }
}

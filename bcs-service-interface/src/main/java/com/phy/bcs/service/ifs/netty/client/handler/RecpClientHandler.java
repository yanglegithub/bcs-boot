package com.phy.bcs.service.ifs.netty.client.handler;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.controller.model.*;
import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import com.phy.bcs.service.ifs.netty.server.handler.FepOverTimeHandler;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

public class RecpClientHandler extends FepOverTimeHandler<ParseRECP> {

    private InetSocketAddress remoteAdress;
    //private InfFileStatusService service;
    private BcsApplicationConfig config;
    //需要发送的文件
    private List<InfFileStatus> files;
    //当前正在发送的文件下标，即第几个文件，fileIndex从0开始
    private int fileIndex = 0;
    //0:建立RECP连接， 1:发送FEP请求包 2:接收FEP请求应答包， 3:发送第一个FEP数据包， 4:发送后续数据包， 5:接收结束确认包， 6:断开RECP连接
    private int step = 0;
    //当前RECP交换的数据包序号
    private int seqNum = 1;
    //当前FEP协议文件id
    private int id;
    //当前文件发送的字节偏移
    private int fileoff = 0;

    public RecpClientHandler(InetSocketAddress remoteAdress, List<InfFileStatus> files){
        this.files = files;
        this.remoteAdress = remoteAdress;
        //service = SpringContextHolder.getBean(InfFileStatusService.class);
        config = SpringContextHolder.getBean(BcsApplicationConfig.class);
    }

    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, ParseRECP msg) {
        if(handleIdlePackage(channelHandlerContext, msg))
            return;
        if (step == 0){
            //如果接收到的包不是相应序号的应答包，则舍弃该包
            if(msg.getFlag() != PackageType.ACK)
                return;
            //if(msg.getSerialNumber() != seqNum)
            //    return;
            step = 1;
            //seqNum++;
            sendFepSYN(channelHandlerContext);
        } else if(step == 1){
            //如果接收到的包不是相应序号的应答包，则舍弃该包
            if(msg.getFlag() != PackageType.ACK || msg.getSerialNumber() != seqNum)
                return;
            step = 2;
            seqNum++;
        } else if(step == 2){
            //如果接收到的包不是相应序号的数据包，则舍弃该包
            if(msg.getFlag() != PackageType.DATA || msg.getSerialNumber() != seqNum)
                return;
            if(msg.getData().getAnswerFEPMode().getNum() < 0){
                sendRecpACK(channelHandlerContext);
                seqNum++;
                closeOrNext(channelHandlerContext);
                return;
            }
            id = msg.getData().getAnswerFEPMode().getID();
            fileoff = msg.getData().getAnswerFEPMode().getNum();
            sendRecpACK(channelHandlerContext);
            step = 3;
            seqNum++;
            sendData(channelHandlerContext);

        } else if(step == 3 || step == 4){
            if(msg.getFlag() != PackageType.ACK || msg.getSerialNumber() != seqNum)
                return;
            //若收到回应，更新文件信息
            if(files.get(fileIndex).getFileContent().length-fileoff < config.getPackgesize()) {
                fileoff = files.get(fileIndex).getFileContent().length;
                step = 5;
                seqNum++;
                return;
            } else {
                fileoff += config.getPackgesize();
                step = 4;
                seqNum++;
                sendData(channelHandlerContext);
            }
        } else if(step == 5){
            if(msg.getFlag() != PackageType.DATA || msg.getSerialNumber() != seqNum)
                return;
            ParseFEP fep = msg.getData();
            if(!(fep.getFlag() == 3) || fep.getFinishFEPMode().getID() != id)
                return;
            sendRecpACK(channelHandlerContext);
            seqNum++;
            closeOrNext(channelHandlerContext);
        } else if(step == 6){
            if(msg.getFlag() != PackageType.ACK || msg.getSerialNumber() != seqNum)
                return;
            channelHandlerContext.close();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        recpRequest(ctx);
    }

    //处理因为网络问题导致服务端的超时重发的包
    public boolean handleIdlePackage(ChannelHandlerContext ctx, ParseRECP msg){
        if(msg.getFlag() == PackageType.ACK && step == 1 && msg.getSerialNumber() == 0){
            sendFepSYN(ctx);
            return true;
        }else if(msg.getFlag() == PackageType.DATA && msg.getSerialNumber() == seqNum-1 && 2 == msg.getData().getFlag()
                && (step == 1 || step == 3)){
            seqNum--;
            sendRecpACK(ctx);
            seqNum++;
            if(step == 3)
                sendData(ctx);
            return true;
        }else if(msg.getFlag() == PackageType.ACK && step == 4 && msg.getSerialNumber() == seqNum - 1){
            sendData(ctx);
            return true;
        }else if(msg.getFlag() == PackageType.DATA && (step == 1 || step == 6) && 3 == msg.getData().getFlag()){
            seqNum--;
            sendRecpACK(ctx);
            seqNum++;
            if(step == 1){
                sendFepSYN(ctx);
            }else if(step == 6){
                sendRecpFIN(ctx);
            }
            return true;
        }
        return false;
    }

    @Override
    public void handleReaderIdle(ChannelHandlerContext ctx){
        if(step == 0){
            recpRequest(ctx);
        }else if(step == 1){
            sendFepSYN(ctx);
        } else if(step == 2){
            //有可能是网络问题，不予处理，让服务端超时？
        } else if(step == 3){
            seqNum--;
            sendRecpACK(ctx);
            seqNum++;
            sendData(ctx);
        } else if(step == 4){
            sendData(ctx);
        } else if(step == 5){
            //有可能是网络问题，不予处理，让服务端超时?
        } else if(step == 6){
            sendRecpFIN(ctx);
        }
    }

    public void recpRequest(ChannelHandlerContext ctx){
        ParseRECP recp = new ParseRECP();
        recp.setFlag(PackageType.SYN);
        try {
            recp.setSourceAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        recp.setSerialNumber(0);
        recp.setReservedBits("1234");
        recp.setAbstractLength(0);
        recp.setAbstractData("");
        recp.setData(null);
        ctx.writeAndFlush(recp);
    }

    public void sendData(ChannelHandlerContext ctx){
        InfFileStatus file = files.get(fileIndex);
        int packgesize = config.getPackgesize();
        byte[] dbyte;
        if(file.getFileContent().length - fileoff < packgesize){
            dbyte = ParseUtil.getBytes(file.getFileContent(), fileoff, file.getFileContent().length - fileoff);
        } else {
            dbyte = ParseUtil.getBytes(file.getFileContent(), fileoff, packgesize);
        }
        //FEP装包
        ParseFEP fep = new ParseFEP();
        fep.setFlag(4);
        DataFEPMode data = new DataFEPMode();
        data.setID(id);
        data.setNum(fileoff);
        try {
            data.setData(new String(dbyte, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        fep.setDataFEPMode(data);
        //RECP装包
        ParseRECP recp = new ParseRECP();
        recp.setFlag(PackageType.DATA);
        try {
            recp.setSourceAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        recp.setSerialNumber(seqNum);
        recp.setReservedBits("");
        recp.setAbstractLength(0);
        recp.setAbstractData("");
        recp.setData(fep);

        ctx.writeAndFlush(recp);
    }

    public void sendFepSYN(ChannelHandlerContext ctx){
        InfFileStatus file = files.get(fileIndex);
        //FEP装包
        ParseFEP fep = new ParseFEP();
        fep.setFlag(1);
        SendFEPMode send = new SendFEPMode();
        send.setFileName(file.getFileName());
        send.setFileLength(file.getLength());
        fep.setSendFEPMode(send);
        //RECP装包
        ParseRECP recp = new ParseRECP();
        recp.setFlag(PackageType.DATA);
        recp.setSerialNumber(seqNum);
        try {
            recp.setSourceAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        recp.setReservedBits("");
        recp.setAbstractLength(0);
        recp.setAbstractData("");
        recp.setData(fep);

        ctx.writeAndFlush(recp);
    }

    public void sendRecpACK(ChannelHandlerContext ctx){
        ParseRECP recp = new ParseRECP();
        recp.setFlag(PackageType.ACK);
        try {
            recp.setSourceAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        recp.setSerialNumber(seqNum);
        recp.setReservedBits("1234");
        recp.setAbstractLength(0);
        recp.setAbstractData("");
        recp.setData(null);
        ctx.writeAndFlush(recp);
    }

    public void sendRecpFIN(ChannelHandlerContext ctx){
        ParseRECP recp = new ParseRECP();
        recp.setFlag(PackageType.FIN);
        try {
            recp.setSourceAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        recp.setSerialNumber(seqNum);
        recp.setReservedBits("");
        recp.setAbstractLength(0);
        recp.setAbstractData("");
        recp.setData(null);
        ctx.writeAndFlush(recp);
    }

    public void closeOrNext(ChannelHandlerContext ctx){
        step = 1;
        id = 0;
        fileoff = 0;
        //保存文件
        InfFileStatus file = files.get(fileIndex);
        file.setSendFinish(1);
        //service.saveOrUpdate(file);
        InfFileStatus.remove(file.getId());

        fileIndex ++;
        if(fileIndex >= files.size()){
            step = 6;
            fileIndex = 0;
            files = null;
            sendRecpFIN(ctx);
        } else {
            sendFepSYN(ctx);
        }
    }
}

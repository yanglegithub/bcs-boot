package com.phy.bcs.service.ifs.netty.server.handler;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.file.service.InfFileStatusService;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.controller.model.*;
import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.List;

@Deprecated
public class RecpClientHandler extends FepOverTimeHandler<ParseRECP>{


    private InetSocketAddress remoteAdress;
    private InfFileStatusService service;
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
        service = SpringContextHolder.getBean(InfFileStatusService.class);
        config = SpringContextHolder.getBean(BcsApplicationConfig.class);
    }

    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, ParseRECP msg) {
        int packtype = handleAnswer(channelHandlerContext, msg);
        if (step == 0 && packtype == 1){
            step = 1;
            sendFepSYN(channelHandlerContext);
        } else if(step == 1 && packtype ==1){
            step = 2;
        } else if(step == 2 && packtype == 2){
            //若接收方不能接收此文件, 则发送下一个文件
            if(msg.getData().getAnswerFEPMode() == null || msg.getData().getAnswerFEPMode().getNum() < 0) {
                closeOrNext(channelHandlerContext, msg.getData().getAnswerFEPMode().getNum()==-1?true:false);
                return;
            }
            id = msg.getData().getAnswerFEPMode().getID();
            fileoff = msg.getData().getAnswerFEPMode().getNum();
            step = 3;
            sendData(channelHandlerContext);
        } else if((step == 3 || step == 4) && packtype == 1){
            //若收到回应，更新文件信息
            if(files.get(fileIndex).getFileContent().length-fileoff < config.getPackgesize()) {
                fileoff = files.get(fileIndex).getFileContent().length;
                step = 5;
                return;
            } else {
                fileoff += config.getPackgesize();
                step = 4;
                sendData(channelHandlerContext);
            }
        } else if(step == 5 && packtype == 2){
            ParseFEP fep = msg.getData();
            if(!fep.getFlag().equals("3") && fep.getFinishFEPMode().getID() != id)
                return;
            closeOrNext(channelHandlerContext, true);
        } else if(step == 6 && packtype == 1){
            channelHandlerContext.close();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        recpRequest(ctx);
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

    /**
     * 用于对维护seqNum字段， 接收处理ACK包 及 发送ACK包
     * @param ctx
     * @param recp
     * @return 1:应答包 2:数据包 0:其它包
     */
    public int handleAnswer(ChannelHandlerContext ctx, ParseRECP recp){

        if(recp.getFlag() == PackageType.ACK && recp.getSerialNumber() == seqNum) {
            seqNum++;
            return 1;
        }
        if(recp.getFlag() == PackageType.DATA && recp.getSerialNumber() == seqNum){
            sendRecpACK(ctx);
            seqNum++;
            return 2;
        }
        return 0;
    }

    public void recpRequest(ChannelHandlerContext ctx){
        ParseRECP recp = new ParseRECP();
        recp.setFlag(PackageType.SYN);
        recp.setSourceAddress(((InetSocketAddress)ctx.channel().localAddress()).getHostName());
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
        fep.setFlag("4");
        DataFEPMode data = new DataFEPMode();
        data.setID(id);
        data.setNum(fileoff);
        try {
            data.setData(new String(dbyte, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //RECP装包
        ParseRECP recp = new ParseRECP();
        recp.setFlag(PackageType.DATA);
        recp.setSourceAddress((((InetSocketAddress)ctx.channel().localAddress()).getHostName()));
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
        fep.setFlag("1");
        SendFEPMode send = new SendFEPMode();
        send.setFileName(file.getFileName());
        send.setFileLength(file.getLength());
        fep.setSendFEPMode(send);
        //RECP装包
        ParseRECP recp = new ParseRECP();
        recp.setFlag(PackageType.DATA);
        recp.setSerialNumber(seqNum);
        recp.setSourceAddress((((InetSocketAddress)ctx.channel().localAddress()).getHostName()));
        recp.setReservedBits("");
        recp.setAbstractLength(0);
        recp.setAbstractData("");
        recp.setData(fep);

        ctx.writeAndFlush(recp);
    }

    public void sendRecpACK(ChannelHandlerContext ctx){
        ParseRECP recp = new ParseRECP();
        recp.setFlag(PackageType.ACK);
        recp.setSourceAddress(((InetSocketAddress)ctx.channel().localAddress()).getHostName());
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
        recp.setSourceAddress(((InetSocketAddress)ctx.channel().localAddress()).getHostName());
        recp.setSerialNumber(seqNum);
        recp.setReservedBits("");
        recp.setAbstractLength(0);
        recp.setAbstractData("");
        recp.setData(null);
        ctx.writeAndFlush(recp);
    }

    public void closeOrNext(ChannelHandlerContext ctx, boolean finished){
        step = 1;
        id = 0;
        fileoff = 0;
        //保存文件
        if(finished) {
            InfFileStatus file = files.get(fileIndex);
            file.setSendFinish(1);
            service.saveOrUpdate(file);
        }

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

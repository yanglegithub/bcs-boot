package com.phy.bcs.service.ifs.netty.server.handler;

import com.phy.bcs.common.util.StringUtils;
import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.config.NetStatus;
import com.phy.bcs.service.ifs.controller.model.*;
import com.phy.bcs.service.ifs.controller.server.ParseModeToByte;
import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import com.phy.bcs.service.ifs.netty.server.RecpServer;
import com.phy.bcs.service.ifs.netty.utils.NumberUtil;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Date;

@Slf4j
@Data
public abstract class RecpServerContext {

    private InetSocketAddress remoteAdress;
    //private InfFileStatusService service;
    private BcsApplicationConfig config;
    //当前正在发送的文件下标，即第几个文件，fileIndex从0开始
    private InfFileStatus file;
    //当前的客户端的ip
    private String ip;
    //0:建立RECP连接， 1:接收FEP请求包 2:发送FEP请求应答包， 3:接收第一个FEP数据包，6:接收后续FEP数据包 4:发送结束确认包， 5:断开RECP连接（接收RECP结束包）
    private int step = 0;
    //当前RECP交换的数据包序号
    private int seqNum = 0;
    //上一次与传送这个文件的时间
    private long timestramp = new Date().getTime();
    private ParseRECP recpmode;
    //连接超时重发包的次数
    private int unconnected_times = 0;

    public RecpServerContext(){
        //service = SpringContextHolder.getBean(InfFileStatusService.class);
        config = SpringContextHolder.getBean(BcsApplicationConfig.class);
    }

    public RecpServerContext(InetSocketAddress remoteAdress){
        this.remoteAdress = remoteAdress;
        //service = SpringContextHolder.getBean(InfFileStatusService.class);
        config = SpringContextHolder.getBean(BcsApplicationConfig.class);
    }

    //客户端数据处理
    protected void handleData(ChannelHandlerContext channelHandlerContext, DatagramPacket packet) {
        remoteAdress = packet.sender();
        ParseRECP msg = null;
        try {
            msg = new ParseRECP(ByteBufUtil.getBytes(packet.content()));
            recpmode = msg;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        if(handleIdlePackage(channelHandlerContext, msg)) {
            //连续中断次数,并添加网络状态
            unconnected_times = 0;
            String ip = getipv4FromRemoteAdress();
            int syscode = getConfig().getSyscodeByIp(ip);
            NetStatus.writeStatus(syscode, true);
            return;
        }

        if(step == 0){
            if(msg.getFlag() != PackageType.SYN)
                return;
            sendRecpACK(channelHandlerContext);
            step = 1;
            seqNum++;
        }else if(step == 1){
            if(msg.getFlag() != PackageType.DATA || msg.getSerialNumber() != seqNum)
                return;
            if(!(1 == msg.getData().getFlag()))
                return;
            log.debug("收到FEP请求包,FEP回应包已发:{}",msg);
            file = generFileStatus(channelHandlerContext, msg.getData());
            sendRecpACK(channelHandlerContext);
            seqNum++;
            step = 2;
            sendFepACK(channelHandlerContext);
        } else if(step == 2){
            if(msg.getFlag() != PackageType.ACK || msg.getSerialNumber() != seqNum)
                return;
            log.debug("收到FEP请求应答包");
            if(file.getRecFinish() == 1)
                step = 1;
            else
                step = 3;
            seqNum++;
        } else if(step == 3 || step == 6){
            if(msg.getFlag() != PackageType.DATA || msg.getSerialNumber() != seqNum)
                return;
            log.debug("收到FEP数据包");
            reciveData(msg.getData());
            sendRecpACK(channelHandlerContext);
            step = 6;
            seqNum++;
            if(file.getRecFinish() == 1){
                sendFepFIN(channelHandlerContext);
                filesend(file);
                step = 4;
            }
        } else if(step == 4){
            if(msg.getFlag() != PackageType.ACK || msg.getSerialNumber() != seqNum)
                return;
            log.debug("接收结束，结束确认包已发送");
            seqNum++;
            step = 1;
        }
        unconnected_times = 0;
        String ip = getipv4FromRemoteAdress();
        int syscode = getConfig().getSyscodeByIp(ip);
        NetStatus.writeStatus(syscode, true);
    }

    //处理因为网络问题导致客户端的超时重发的包
    public boolean handleIdlePackage(ChannelHandlerContext ctx, ParseRECP msg){
        if(msg.getFlag() == PackageType.SYN && step == 1){
            seqNum--;
            sendRecpACK(ctx);
            seqNum++;
            return true;
        }else if(msg.getFlag() == PackageType.DATA && step == 2 && 1 == msg.getData().getFlag()){
            seqNum--;
            sendRecpACK(ctx);
            seqNum++;
            sendFepACK(ctx);
            return true;
        }else if(msg.getFlag() == PackageType.DATA && (step == 6 || step == 4) && msg.getSerialNumber() == seqNum - 1){
            seqNum--;
            sendRecpACK(ctx);
            seqNum++;
            return true;
        }

        return false;
    }

    //超时重发处理
    public void handleReaderIdle(ChannelHandlerContext ctx){
        int sec = config.getTimeout();
        if(new Date().getTime() - timestramp < sec * 1000 || sec == 0)
            return;
        //连续超时次数+1，并添加网络状态
        unconnected_times++;
        String ip = getipv4FromRemoteAdress();
        int syscode = getConfig().getSyscodeByIp(ip);
        NetStatus.writeStatus(syscode, false);

        log.debug("ip;{} 读超时,现在处于step={}", ip, step);
        if(step == 1){
            if(seqNum == 1) {
                seqNum--;
                sendRecpACK(ctx);
                seqNum++;
            }
        }else if(step == 2){
            seqNum--;
            sendRecpACK(ctx);
            seqNum++;
            sendFepACK(ctx);
        }else if(step == 3){
            //可能是网络状态不好，让客户端超时
        }else if(step == 6){
            seqNum--;
            sendRecpACK(ctx);
            seqNum++;
        }else if(step == 4){
            seqNum--;
            sendRecpACK(ctx);
            seqNum++;
            sendFepFIN(ctx);
        }
    }


    public InfFileStatus generFileStatus(ChannelHandlerContext ctx, ParseFEP fep){
        SendFEPMode sendMode = fep.getSendFEPMode();
        //InfFileStatus olc = service.findOneByFilename(sendMode.getFileName().trim());
        InfFileStatus olc = InfFileStatus.getByFileName(sendMode.getFileName().trim());
        if (olc != null){
            if(olc.getFileContent() == null)
                olc.setFileContent(new byte[0]);
            return olc;
        }

        //创建一条filestatus数据
        InfFileStatus newFile = new InfFileStatus();
        newFile.setFileName(sendMode.getFileName().trim());
        newFile.setLength(sendMode.getFileLength());
        newFile.setFileContent(new byte[0]);
        //从哪个系统接收的
        String ip = remoteAdress.getAddress().getHostAddress();
        newFile.setFromSystem(config.getSyscodeByIp(ip));
        newFile.setFromProto("RECP");
        //到哪个系统去
        newFile.setToSystem(sysTransfor(ctx, sendMode));
        newFile.setToProto(protoTransfor(ctx, sendMode));
        newFile.setRecFinish(0);
        newFile.setSendFinish(0);
        newFile.setTransTimes(0);
        newFile.setCreateTime(new Date());
        //service.save(newFile);
        InfFileStatus.addInfFile(newFile);
        return newFile;

    }


    public void reciveData(ParseFEP fep){
        int packgesize = config.getPackgesize();
        DataFEPMode data = fep.getDataFEPMode();
        if(data.getID() != file.getId() || data.getNum() != (file.getFileContent().length))
            return;
        byte[] dbyte = data.getData();
        file.setFileContent(ParseUtil.byteMerger(file.getFileContent(), dbyte));

        if(dbyte.length < packgesize){
            file.setRecFinish(1);
            //service.saveOrUpdate(file);
        }
    }

    public void sendFepACK(ChannelHandlerContext ctx){
        //FEP装包
        ParseFEP fep = new ParseFEP();
        fep.setFlag(2);
        AnswerFEPMode mode = new AnswerFEPMode();
        mode.setID(file.getId());
        mode.setFileName(file.getFileName());
        mode.setNum(file.getRecFinish()==0?(file.getFileContent().length):-1);
        fep.setAnswerFEPMode(mode);
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

        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(ParseModeToByte.parseRecpTo(recp)), remoteAdress));
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
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(ParseModeToByte.parseRecpTo(recp)), remoteAdress));
    }

    public void sendFepFIN(ChannelHandlerContext ctx){
        ParseFEP fep = new ParseFEP();
        fep.setFlag(3);
        FinishFEPMode mode = new FinishFEPMode();
        mode.setID(file.getId());
        fep.setFinishFEPMode(mode);

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
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(ParseModeToByte.parseRecpTo(recp)), remoteAdress));
    }

    //转发逻辑，返回要转发的系统的代码
    public abstract int sysTransfor(ChannelHandlerContext ctx, SendFEPMode fep);

    //转发逻辑，返回转发要用的协议
    public abstract String protoTransfor(ChannelHandlerContext ctx, SendFEPMode fep);

    //转发逻辑，执行转发操作,在RECP发送了FEP结束包的时候调用
    public abstract void filesend(InfFileStatus file);

    public String getipv4FromRemoteAdress(){
        String ip = remoteAdress.getAddress().getHostAddress();
        byte[] ipbytes = ParseModeToByte.getIpbyteFromStr(ip);
        String ipstr = "";
        for(int i=0; i<4; i++){
            ipstr += (i==0?String.valueOf(ipbytes[i]):("."+String.valueOf(0x000000FF & ipbytes[i])));
        }
        return ipstr;
    }
}

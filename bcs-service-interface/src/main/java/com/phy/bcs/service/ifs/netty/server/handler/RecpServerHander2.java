package com.phy.bcs.service.ifs.netty.server.handler;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.controller.model.*;
import com.phy.bcs.service.ifs.controller.server.ParseModeToByte;
import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public abstract class RecpServerHander2 extends FepOverTimeHandler<DatagramPacket>{


    private InetSocketAddress remoteAdress;
    //private InfFileStatusService service;
    private BcsApplicationConfig config;
    //需要接收的文件
    private Map<String, Map<String, Object>> ipfile;
    //当前正在发送的文件下标，即第几个文件，fileIndex从0开始
    private InfFileStatus file;
    //当前的客户端的ip
    private String ip;
    //0:建立RECP连接， 1:接收FEP请求包 2:发送FEP请求应答包， 3:接收第一个FEP数据包，6:接收后续FEP数据包 4:发送结束确认包， 5:断开RECP连接（接收RECP结束包）
    private int step = 0;
    //当前RECP交换的数据包序号
    private int seqNum = 1;
    //上一次与传送这个文件的时间
    private long timestramp = new Date().getTime();

    public RecpServerHander2(InetSocketAddress remoteAdress){
        this.remoteAdress = remoteAdress;
        //service = SpringContextHolder.getBean(InfFileStatusService.class);
        config = SpringContextHolder.getBean(BcsApplicationConfig.class);
    }

    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, DatagramPacket packet) {
        remoteAdress = packet.sender();
        ParseRECP msg = null;
        try {
            msg = new ParseRECP(ByteBufUtil.getBytes(packet.content()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Map<String, Object> context = ipfile.get(msg.getSourceAddress());
        changeContext(msg.getSourceAddress());
        if(msg.getFlag() == PackageType.SYN){
            if(context == null){
                Map<String, Object> connect = new HashMap<>();
                connect.put("step", 1);
                connect.put("seqNum", 1);
                connect.put("timestramp", new Date().getTime());
                connect.put("file", null);
                connect.put("remoteAdress", remoteAdress);
                ipfile.put(msg.getSourceAddress(), connect);
                sendRecpACK(channelHandlerContext);
                return;
            } else {
                return;
            }
        }else if(msg.getFlag() == PackageType.FIN){
            //service.saveOrUpdate(file);
            ipfile.remove(ip);
        }

        if(step == 1){
            if(msg.getFlag() != PackageType.DATA && msg.getSerialNumber() != seqNum)
                return;
            if(!"1".equals(msg.getData().getFinishFEPMode()))
                return;
            file = generFileStatus(channelHandlerContext, msg.getData());
            ipfile.get(ip).put("file", file);
            sendRecpACK(channelHandlerContext);
            seqNum++;
            step = 2;
            sendFepACK(channelHandlerContext);
        } else if(step == 2){
            if(msg.getFlag() != PackageType.ACK && msg.getSerialNumber() != seqNum)
                return;
            if(file.getRecFinish() == 1)
                step = 1;
            else
                step = 3;
            seqNum++;
        } else if(step == 3 || step == 6){
            if(msg.getFlag() != PackageType.DATA && msg.getSerialNumber() != seqNum)
                return;
            reciveData(msg.getData());
            sendRecpACK(channelHandlerContext);
            step = 6;
            seqNum++;
            if(file.getRecFinish() == 1){
                sendFepFIN(channelHandlerContext);
                step = 4;
            }
        } else if(step == 4){
            if(msg.getFlag() != PackageType.ACK && msg.getSerialNumber() != seqNum)
                return;
            seqNum++;
            step = 1;
        }
    }


    @Override
    public void handleReaderIdle(ChannelHandlerContext ctx){
        Map<String, Object> old = ipfile.get(this.ip);
        if(old != null){
            old.put("step", step);
            old.put("seqNum", seqNum);
            old.put("timestramp", timestramp);
            old.put("file", file);
            old.put("remoteAdress", remoteAdress);
        }
        int sec = config.getTimeout();
        for (Map.Entry<String, Map<String, Object>> entry : ipfile.entrySet()){
            Map<String, Object> map = entry.getValue();
            timestramp = (long) map.get("timestramp");
            if(timestramp - new Date().getTime() < sec * 1000)
                continue;
            ip = (String) map.get("ip");
            file = (InfFileStatus) map.get("file");
            step = (int) map.get("step");
            seqNum = (int) map.get("seqNum");
            remoteAdress = (InetSocketAddress) map.get("remoteAdress");
            if(step == 1){
                sendRecpACK(ctx);
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
        ip = (String) old.get("ip");
        file = (InfFileStatus) old.get("file");
        timestramp = (long) old.get("timestramp");
        step = (int) old.get("step");
        seqNum = (int) old.get("seqNum");
        remoteAdress = (InetSocketAddress) old.get("remoteAdress");

    }


    public InfFileStatus generFileStatus(ChannelHandlerContext ctx, ParseFEP fep){
        SendFEPMode sendMode = fep.getSendFEPMode();
        //InfFileStatus olc = service.findOneByFilename(sendMode.getFileName().trim());
        InfFileStatus olc = InfFileStatus.getByFileName(sendMode.getFileName().trim());
        if (olc != null)
            return olc;

        //创建一条filestatus数据
        InfFileStatus newFile = new InfFileStatus();
        newFile.setFileName(sendMode.getFileName().trim());
        newFile.setLength(sendMode.getFileLength());
        newFile.setFileContent(new byte[0]);
        //从哪个系统接收的
        String ip = remoteAdress.getHostName();
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

    public void changeContext(String ip){
        Map<String, Object> old = this.ip!=null?ipfile.get(this.ip):null;
        if(old != null){
            old.put("step", step);
            old.put("seqNum", seqNum);
            old.put("timestramp", timestramp);
            old.put("file", file);
            //old.put("remoteAdress", remoteAdress);
        }
        Map<String, Object> context = ipfile.get(ip);
        if(context != null){
            step = (int) context.get("step");
            seqNum = (int) context.get("seqNum");
            timestramp = (long) context.get("timestramp");
            file = (InfFileStatus) context.get("file");
            this.ip = ip;
            timestramp = new Date().getTime();
        }
    }


    public void reciveData(ParseFEP fep){
        int packgesize = config.getPackgesize();
        DataFEPMode data = fep.getDataFEPMode();
        if(data.getID() != file.getId() || data.getNum() != file.getFileContent().length)
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
        mode.setNum(file.getRecFinish()==0?file.getFileContent().length:-1);
        fep.setAnswerFEPMode(mode);
        //RECP装包
        ParseRECP recp = new ParseRECP();
        recp.setFlag(PackageType.DATA);
        recp.setSerialNumber(seqNum);
        recp.setSourceAddress(((InetSocketAddress)ctx.channel().localAddress()).getHostName());
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
        recp.setSourceAddress(((InetSocketAddress)ctx.channel().localAddress()).getHostName());
        recp.setSerialNumber(seqNum);
        recp.setReservedBits("");
        recp.setAbstractLength(0);
        recp.setAbstractData("");
        recp.setData(fep);
        ctx.writeAndFlush(recp);
    }

    public abstract int sysTransfor(ChannelHandlerContext ctx, SendFEPMode fep);

    public abstract String protoTransfor(ChannelHandlerContext ctx, SendFEPMode fep);
}

package com.phy.bcs.service.ifs.netty.server.handler;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.controller.model.*;
import com.phy.bcs.service.ifs.controller.server.ParseModeToByte;
import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RecpServerHandler extends FepOverTimeHandler<DatagramPacket>{


    private InetSocketAddress remoteAdress;
    //private InfFileStatusService service;
    private BcsApplicationConfig config;
    private Class<? extends RecpServerContext> type;
    //需要接收的文件
    private Map<String, RecpServerContext> ipcontext = new HashMap<>();

    public RecpServerHandler(Class<? extends RecpServerContext> type){
        this.type = type;
        //service = SpringContextHolder.getBean(InfFileStatusService.class);
        config = SpringContextHolder.getBean(BcsApplicationConfig.class);
    }

    public RecpServerContext createContext(){
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, DatagramPacket packet) {
        remoteAdress = packet.sender();
        ParseRECP msg = null;
        handleReaderIdle(channelHandlerContext);
        try {
            msg = new ParseRECP(ByteBufUtil.getBytes(packet.content()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        RecpServerContext context = ipcontext.get(msg.getSourceAddress());
        if(context != null)
            context.setTimestramp(new Date().getTime());
        if(msg.getFlag() == PackageType.SYN){
            if(context == null){
                log.debug("收到RECP连接请求,ip:{}",msg.getSourceAddress());
                RecpServerContext newconnect = createContext();
                newconnect.setIp(msg.getSourceAddress());
                newconnect.setRemoteAdress(packet.sender());
                ipcontext.put(msg.getSourceAddress(), newconnect);
                context = newconnect;
            }
        }else if(msg.getFlag() == PackageType.FIN){
            if(context != null && context.getSeqNum() == msg.getSerialNumber()){
                //service.saveOrUpdate(context.getFile());
                ipcontext.remove(context.getIp());
                log.debug("RECP连接请求结束，ip:{}",context.getIp());
                sendRecpACK(channelHandlerContext, msg.getSerialNumber());
                return;
            }else if(context == null){
                sendRecpACK(channelHandlerContext, msg.getSerialNumber());
            }
        }
        if(context != null)
            context.handleData(channelHandlerContext, packet);
        remoteAdress = null;
    }


    @Override
    public void handleReaderIdle(ChannelHandlerContext ctx){
        for (Map.Entry<String, RecpServerContext> entry : ipcontext.entrySet()){
            //若在处理时遍历到自己的ip，则跳过
            if(remoteAdress != null){
                int remote = ParseUtil.bytesToInt2(ParseModeToByte.getIpbyteFromStr(remoteAdress.getAddress().getHostAddress()), 0);
                int thisip = ParseUtil.bytesToInt2(ParseModeToByte.getIpbyteFromStr(entry.getKey()),0);
                if(remote == thisip)
                    continue;
            }
            RecpServerContext context = entry.getValue();
            context.handleReaderIdle(ctx);
            if (context.getUnconnected_times() >= config.getReconnectTimes()){
                ipcontext.remove(context.getIp());
                log.debug("ip:{} RECP连接超时,请重新发送RECP连接", context.getIp());
            }
        }
    }

    public void sendRecpACK(ChannelHandlerContext ctx, int seqNum){
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
}

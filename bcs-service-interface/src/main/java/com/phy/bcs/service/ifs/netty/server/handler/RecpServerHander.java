package com.phy.bcs.service.ifs.netty.server.handler;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.file.service.InfFileStatusService;
import com.phy.bcs.service.ifs.config.MyAppllicationConfig;
import com.phy.bcs.service.ifs.controller.model.*;
import com.phy.bcs.service.ifs.controller.server.ParseModeToByte;
import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RecpServerHander extends FepOverTimeHandler<DatagramPacket>{


    private InetSocketAddress remoteAdress;
    private InfFileStatusService service;
    private MyAppllicationConfig config;
    private Class<? extends RecpServerContext> type;
    //需要接收的文件
    private Map<String, RecpServerContext> ipcontext;

    public RecpServerHander(Class<? extends RecpServerContext> type){
        this.type = type;
        service = SpringContextHolder.getBean(InfFileStatusService.class);
        config = SpringContextHolder.getBean(MyAppllicationConfig.class);
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
                RecpServerContext newconnect = createContext();
                newconnect.setIp(msg.getSourceAddress());
                newconnect.setRemoteAdress(packet.sender());
                ipcontext.put(msg.getSourceAddress(), newconnect);
                newconnect.sendRecpACK(channelHandlerContext);
                return;
            } else {
                return;
            }
        }else if(msg.getFlag() == PackageType.FIN){
            service.saveOrUpdate(context.getFile());
            ipcontext.remove(context.getIp());
        }
    }


    @Override
    public void handleReaderIdle(ChannelHandlerContext ctx){
        for (Map.Entry<String, RecpServerContext> entry : ipcontext.entrySet()){
            RecpServerContext context = entry.getValue();
            context.handleReaderIdle(ctx);
        }
    }
}

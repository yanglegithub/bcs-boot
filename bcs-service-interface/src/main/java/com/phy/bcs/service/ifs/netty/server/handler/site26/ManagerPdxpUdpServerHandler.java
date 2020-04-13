package com.phy.bcs.service.ifs.netty.server.handler.site26;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.config.Constants;
import com.phy.bcs.service.ifs.netty.client.PdxpUdpClient;
import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessage;
import com.phy.bcs.service.ifs.netty.utils.NumberUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.math.BigInteger;
import java.net.InetSocketAddress;

public class ManagerPdxpUdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    InetSocketAddress reomteAdress;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        reomteAdress = packet.sender();
        ByteBuf buf = packet.content();
        if(buf.readableBytes() < 32){
            return;
        }
        int ver = buf.readByte();
        ByteBuf mid = buf.readBytes(2);
        ByteBuf sid = buf.readBytes(4);
        ByteBuf did = buf.readBytes(4);
        ByteBuf bid= buf.readBytes(4);
        int number = buf.readInt();
        byte flag = buf.readByte();
        ByteBuf res = buf.readBytes(4);
        ByteBuf date = buf.readBytes(2);
        ByteBuf time = buf.readBytes(4);
        ByteBuf length = buf.readBytes(2);
        ByteBuf data = buf.readBytes(buf.readableBytes());
        PdxpMessage pdxp = PdxpMessage.builder()
                .ver(ver)
                .mid(ByteBufUtil.getBytes(mid))
                .sid(ByteBufUtil.getBytes(sid))
                .did(ByteBufUtil.getBytes(did))
                .bid(ByteBufUtil.getBytes(bid))
                .number(number)
                .flag(flag)
                .reserve(ByteBufUtil.getBytes(res))
                .date(new BigInteger(ByteBufUtil.getBytes(date)).intValue())
                .time(new BigInteger(ByteBufUtil.getBytes(time)).intValue())
                .l(new BigInteger(ByteBufUtil.getBytes(length)).intValue())
                .data(ByteBufUtil.getBytes(data))
                .build();
        handlerData(ctx, pdxp);
    }

    public void handlerData(ChannelHandlerContext ctx, PdxpMessage msg){
        String ip = reomteAdress.getAddress().getHostAddress();
        BcsApplicationConfig config = SpringContextHolder.getBean(BcsApplicationConfig.class);
        int code = config.getSyscodeByIp(ip);
        if(code == Constants.TSM_SYSTEM){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int tocode = config.getSystemCodeByMid(NumberUtil.byte2ToUnsignedShort(msg.getMid()));
                    tocode = tocode == -1?Constants.TFC_SYSTEM:tocode;
                    String toip = "";
                    int toport = 0;
                    if(tocode == Constants.TFC_SYSTEM){
                        toip = config.getTfcSystem().getIp();
                        toport = config.getTfcSystem().getPdxpPort();
                    }else{
                        toip = config.getFfocSystem().getIp();
                        toport = config.getFfocSystem().getPdxpPort();
                    }
                    PdxpUdpClient client = new PdxpUdpClient(toip, toport);
                    try {
                        client.send(msg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }else if(code == Constants.FFO_SYSTEM){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PdxpUdpClient client = new PdxpUdpClient(config.getTsmSystem().getIp(), config.getTsmSystem().getPdxpPort());
                    try {
                        client.send(msg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}

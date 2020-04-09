package com.phy.bcs.service.ifs.netty.codec.udfUdp;

import com.phy.bcs.service.ifs.netty.codec.udf.UdfMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;

public class UdfMessageDatagramEncoder extends MessageToMessageEncoder<UdfMessage> {
    private final InetSocketAddress remoteAddress;

    public UdfMessageDatagramEncoder(InetSocketAddress adress){
        this.remoteAddress = adress;
    }
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, UdfMessage msg, List<Object> list) throws Exception {
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer();

        byteBuf.writeBytes(msg.getSid());
        byteBuf.writeBytes(msg.getDid());
        byteBuf.writeBytes(msg.getMid());
        byteBuf.writeBytes(msg.getBid());
        byteBuf.writeBytes(msg.getRes());
        byteBuf.writeBytes(msg.getJs());
        byteBuf.writeBytes(msg.getLen());
        byteBuf.writeBytes(msg.getData());

        list.add(new DatagramPacket(byteBuf, this.remoteAddress));
    }
}

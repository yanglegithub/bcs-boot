package com.phy.bcs.service.ifs.netty.codec.udf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class UdfMessageToByteEncoder extends MessageToByteEncoder<UdfMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, UdfMessage msg, ByteBuf byteBuf) throws Exception {
        System.out.println("Encode msg is : " + msg);
        byteBuf.writeBytes(msg.getSid());
        byteBuf.writeBytes(msg.getDid());
        byteBuf.writeBytes(msg.getMid());
        byteBuf.writeBytes(msg.getBid());
        byteBuf.writeBytes(msg.getRes());
        byteBuf.writeBytes(msg.getJs());
        byteBuf.writeBytes(msg.getLen());
        byteBuf.writeBytes(msg.getData());
    }
}

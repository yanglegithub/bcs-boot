package com.phy.bcs.service.ifs.netty.codec.recp;

import com.phy.bcs.service.ifs.controller.model.ParseRECP;
import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.math.BigInteger;
import java.util.List;

public class RecpMessageDatagramDecoder extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        ByteBuf buf = msg.content();
        if(buf.readableBytes() < 32){
            return;
        }
        byte[] bytes = ByteBufUtil.getBytes(buf);
        ParseRECP recp = new ParseRECP(bytes);
        out.add(recp);
        System.out.println("Decode msg is : " + recp);
    }
}

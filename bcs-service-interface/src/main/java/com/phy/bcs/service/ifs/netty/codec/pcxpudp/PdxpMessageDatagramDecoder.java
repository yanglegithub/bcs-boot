package com.phy.bcs.service.ifs.netty.codec.pcxpudp;

import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.math.BigInteger;
import java.util.List;

public class PdxpMessageDatagramDecoder extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        ByteBuf buf = msg.content();
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
        out.add(pdxp);
        System.out.println("Decode msg is : " + pdxp);
    }
}

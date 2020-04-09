package com.phy.bcs.service.ifs.netty.codec.udfUdp;

import com.phy.bcs.service.ifs.netty.codec.udf.UdfMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class UdfMessageDatagramDecoder extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> list) throws Exception {
        ByteBuf byteBuf = datagramPacket.content();
        if (byteBuf.readableBytes() < 24) {
            return;
        }
        try {
            ByteBuf sid = byteBuf.readBytes(4);
            ByteBuf did = byteBuf.readBytes(4);
            ByteBuf mid = byteBuf.readBytes(2);
            ByteBuf bid = byteBuf.readBytes(4);
            ByteBuf res = byteBuf.readBytes(4);
            ByteBuf js = byteBuf.readBytes(4);
            ByteBuf len = byteBuf.readBytes(2);
            byte[] data = new byte[(byteBuf.readableBytes())];
            byteBuf.readBytes(data);

            UdfMessage message = UdfMessage.builder()
                    .sid(ByteBufUtil.getBytes(sid))
                    .did(ByteBufUtil.getBytes(did))
                    .mid(ByteBufUtil.getBytes(mid))
                    .bid(ByteBufUtil.getBytes(bid))
                    .res(ByteBufUtil.getBytes(res))
                    .js(ByteBufUtil.getBytes(js))
                    .len(ByteBufUtil.getBytes(len))
                    .data(data)
                    .build();
            list.add(message);
            System.out.println("Decode msg is : " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

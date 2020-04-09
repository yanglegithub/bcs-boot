package com.phy.bcs.service.ifs.netty.codec.pdxp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PdxpMessageToByteEncoder extends MessageToByteEncoder<PdxpMessage> {
    /**
     * Encode a message into a {@link ByteBuf}. This method will be called for each written message that can be handled
     * by this encoder.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link MessageToByteEncoder} belongs to
     * @param msg the message to encode
     * @param out the {@link ByteBuf} into which the encoded message will be written
     * @throws Exception is thrown if an error occurs
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, PdxpMessage msg, ByteBuf out) throws Exception {
        out.writeInt(msg.getVer());
        out.writeBytes(msg.getMid());
        out.writeBytes(msg.getSid());
        out.writeBytes(msg.getDid());
        out.writeBytes(msg.getBid());
        out.writeInt(msg.getNumber());
        out.writeByte(msg.getFlag());
        out.writeBytes(msg.getReserve());
        out.writeInt(msg.getDate());
        out.writeInt(msg.getTime());
        out.writeInt(msg.getL());
        out.writeBytes(msg.getData());
    }
}

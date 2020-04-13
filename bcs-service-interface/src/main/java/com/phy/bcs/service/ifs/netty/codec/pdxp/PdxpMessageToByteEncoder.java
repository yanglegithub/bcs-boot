package com.phy.bcs.service.ifs.netty.codec.pdxp;

import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import com.phy.bcs.service.ifs.netty.utils.NumberUtil;
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
        byte[] src = preData(msg);
        out.writeByte(0x7E);
        for (int i=0; i < src.length; i++){
            if(src[i] == 0x7E){
                out.writeShort(0x7D5E);
            }else if(src[i] == 0x7D){
                out.writeShort(0x7D5D);
            }else{
                out.writeByte(src[i]);
            }
        }
        out.writeByte(0x7E);

    }

    private byte[] preData(PdxpMessage pdxp){
        byte[] src = new byte[32 + pdxp.getData().length];
        ParseUtil.setBytes(src, 0, new byte[]{(byte) pdxp.getVer()});
        ParseUtil.setBytes(src, 1, pdxp.getMid());
        ParseUtil.setBytes(src, 3, pdxp.getSid());
        ParseUtil.setBytes(src, 7, pdxp.getDid());
        ParseUtil.setBytes(src, 11, pdxp.getBid());
        ParseUtil.setBytes(src, 15, NumberUtil.intToByte4(pdxp.getNumber()));
        ParseUtil.setBytes(src, 19, new byte[]{pdxp.getFlag()});
        ParseUtil.setBytes(src, 20, pdxp.getReserve());
        ParseUtil.setBytes(src, 24, NumberUtil.unsignedShortToByte2(pdxp.getDate()));
        ParseUtil.setBytes(src, 26, NumberUtil.intToByte4(pdxp.getTime()));
        ParseUtil.setBytes(src, 30, NumberUtil.unsignedShortToByte2(pdxp.getTime()));
        ParseUtil.setBytes(src, 32, pdxp.getData());
        return src;
    }
}

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
        System.arraycopy(new byte[]{(byte) pdxp.getVer()}, 0, src, 0, 1);
        System.arraycopy(pdxp.getMid(), 0, src, 1, 2);
        System.arraycopy(pdxp.getSid(), 0, src, 3, 4);
        System.arraycopy(pdxp.getDid(), 0, src, 7, 4);
        System.arraycopy(pdxp.getBid(), 0, src, 11, 4);
        System.arraycopy(NumberUtil.intToByte4(pdxp.getNumber()), 0, src, 15, 4);
        System.arraycopy(new byte[]{pdxp.getFlag()}, 0, src, 19, 1);
        System.arraycopy(pdxp.getReserve(), 0, src, 20, 4);
        System.arraycopy(NumberUtil.unsignedShortToByte2(pdxp.getDate()), 0, src, 24, 2);
        System.arraycopy(NumberUtil.intToByte4(pdxp.getTime()), 0, src, 26, 4);
        System.arraycopy(NumberUtil.unsignedShortToByte2(pdxp.getL()), 0, src, 30, 2);
        System.arraycopy(pdxp.getData(), 0, src, 32, pdxp.getData().length);
        return src;
    }
}

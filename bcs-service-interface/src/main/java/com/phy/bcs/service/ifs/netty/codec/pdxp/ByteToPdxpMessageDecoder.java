package com.phy.bcs.service.ifs.netty.codec.pdxp;

import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import com.phy.bcs.service.ifs.netty.utils.NumberUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.ArrayList;
import java.util.List;

public class ByteToPdxpMessageDecoder extends ByteToMessageDecoder {
    /**
     * Decode the from one {@link ByteBuf} to an other. This method will be called till either the input
     * {@link ByteBuf} has nothing to read when return from this method or till nothing was read from the input
     * {@link ByteBuf}.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param in  the {@link ByteBuf} from which to read data
     * @param out the {@link List} to which decoded messages should be added
     * @throws Exception is thrown if an error occurs
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readableBytes();
        ByteBuf byteBuf = in.readBytes(length);
        byte[] bytes = ByteBufUtil.getBytes(byteBuf);
        byte[] preDecode = preDecode(bytes);
        PdxpMessage pdxpMessage = PdxpMessage.builder().build();
        pdxpMessage.setVer(preDecode[0] & 0b11000000);
        pdxpMessage.setMid(ParseUtil.strChange(preDecode, 1, 2+1));
        pdxpMessage.setSid(ParseUtil.strChange(preDecode, 3, 4+3));
        pdxpMessage.setDid(ParseUtil.strChange(preDecode, 7, 4+7));
        pdxpMessage.setBid(ParseUtil.strChange(preDecode, 11, 4+11));
        pdxpMessage.setNumber(NumberUtil.byte4ToInt(ParseUtil.strChange(preDecode, 15, 4+15), 0));
        pdxpMessage.setFlag(preDecode[19]);
        pdxpMessage.setReserve(ParseUtil.strChange(preDecode, 20, 4+20));
        pdxpMessage.setDate(NumberUtil.byte2ToUnsignedShort(ParseUtil.strChange(preDecode, 24, 2+24)));
        pdxpMessage.setTime(NumberUtil.byte4ToInt(ParseUtil.strChange(preDecode, 26, 4+26), 0));
        pdxpMessage.setL(NumberUtil.byte2ToUnsignedShort(ParseUtil.strChange(preDecode, 30, 2+30)));
        pdxpMessage.setData(ParseUtil.strChange(preDecode, 32, preDecode.length));
        out.add(pdxpMessage);
    }

    private byte[] preDecode(byte[] bytes) {
        ArrayList<Integer> headIndexs = new ArrayList<>();
        ArrayList<Integer> index1 = new ArrayList<>();
        ArrayList<Integer> index2 = new ArrayList<>();
        for (int i = 0; i < bytes.length - 1; i++) {
            if (bytes[i] == 0x7E) {
                headIndexs.add(i);
            }
            if (bytes[i] == 0x7D) {
                if (bytes[i + 1] == 0x5E) {
                    index1.add(i);
                }
                if (bytes[i + 1] == 0x5D) {
                    index2.add(i);
                }
            }
        }
        for (int i : headIndexs) {
            for (int j = i; j < bytes.length - 2; j++) {
                bytes[j] = bytes[j + 1];
            }
        }
        for (int i : index1) {
            for (int j = i; j < bytes.length - 2; j++) {
                bytes[j] = 0x7E;
            }
        }
        for (int i : index2) {
            for (int j = i; j < bytes.length - 2; j++) {
                bytes[j + 1] = bytes[j + 2];
            }
        }
        return bytes;
    }

}

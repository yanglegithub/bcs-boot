package com.phy.bcs.service.ifs.netty.codec.pdxp;

import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import com.phy.bcs.service.ifs.netty.utils.NumberUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ByteToPdxpMessageDecoder extends ByteToMessageDecoder {
    private boolean isover = true;
    private byte[] previous;
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
        while (true) {
            if(bytes == null || bytes.length == 0)
                return;
            SepPack pack = getFirst(bytes);
            if(pack == null)
                return;
            bytes = pack.getReserve();
            byte[] preDecode = preDecode(pack.getTarget());
            PdxpMessage pdxpMessage = PdxpMessage.builder().build();
            pdxpMessage.setVer(preDecode[0] & 0b11000000);
            pdxpMessage.setMid(ParseUtil.strChange(preDecode, 1, 2 + 1));
            pdxpMessage.setSid(ParseUtil.strChange(preDecode, 3, 4 + 3));
            pdxpMessage.setDid(ParseUtil.strChange(preDecode, 7, 4 + 7));
            pdxpMessage.setBid(ParseUtil.strChange(preDecode, 11, 4 + 11));
            pdxpMessage.setNumber(NumberUtil.byte4ToInt(ParseUtil.strChange(preDecode, 15, 4 + 15), 0));
            pdxpMessage.setFlag(preDecode[19]);
            pdxpMessage.setReserve(ParseUtil.strChange(preDecode, 20, 4 + 20));
            pdxpMessage.setDate(NumberUtil.byte2ToUnsignedShort(ParseUtil.strChange(preDecode, 24, 2 + 24)));
            pdxpMessage.setTime(NumberUtil.byte4ToInt(ParseUtil.strChange(preDecode, 26, 4 + 26), 0));
            pdxpMessage.setL(NumberUtil.byte2ToUnsignedShort(ParseUtil.strChange(preDecode, 30, 2 + 30)));
            pdxpMessage.setData(ParseUtil.strChange(preDecode, 32, preDecode.length));
            out.add(pdxpMessage);
        }
    }

    private byte[] preDecode(byte[] bytes) {
        ArrayList<Integer> index1 = new ArrayList<>();
        ArrayList<Integer> index2 = new ArrayList<>();
        ArrayList<Integer> index = new ArrayList<>();
        List<byte[]> bytearrays = new ArrayList<>();
        bytes = ParseUtil.strChange(bytes, 1, bytes.length - 1);
        index.add(bytes.length);
        for (int i = 0; i < bytes.length - 1; i++) {
            if (bytes[i] == 0x7D) {
                if (bytes[i + 1] == 0x5E) {
                    index1.add(i);
                }
                if (bytes[i + 1] == 0x5D) {
                    index2.add(i);
                }
            }
        }
        for (int i : index1) {
            bytes[i] = 0x7E;
            index.add(i+1);
        }
        for (int i : index2) {
            index.add(i+1);
        }
        index.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        for (int i =0;i < index.size(); i++){
            int idx = index.get(i);
            if(i == 0)
                bytearrays.add(ParseUtil.strChange(bytes, 0, idx));
            else
                bytearrays.add(ParseUtil.strChange(bytes, index.get(i-1)+1, idx));
        }
        return merger(bytearrays);
    }

    public byte[] merger(List<byte[]> bytearrays){
        int length = 0;
        int despos = 0;
        for(byte[] b : bytearrays)
            length += b.length;
        byte[] des = new byte[length];
        for(byte[] b : bytearrays){
            System.arraycopy(b, 0, des, despos, b.length);
            despos += b.length;
        }
        return des;
    }

    public SepPack getFirst(byte[] bytes){
        SepPack pack = new SepPack();
        int start = 0;
        int end = 0;
        boolean isbegin = false;
        boolean tisover = false;
        if(isover){
            for (int i = 0; i < bytes.length; i++){
                if(bytes[i] == 0x7E && !isbegin){
                    start = i;
                    isbegin = true;
                }else if(bytes[i] == 0x7E && isbegin){
                    end = i;
                    tisover = true;
                    break;
                }
            }
            if(isbegin&&tisover){
                pack.setTarget(ParseUtil.strChange(bytes, start, end+1));
                if(end >= bytes.length-1)
                    pack.setReserve(null);
                pack.setReserve(ParseUtil.strChange(bytes, end+1, bytes.length));
                return pack;
            }else if(isbegin && !tisover){
                isover = false;
                previous = ParseUtil.strChange(bytes, start, bytes.length);
                return null;
            }else{
                return null;
            }
        }else{
            for (int i = 0; i < bytes.length; i++){
                if(bytes[i] == 0x7E){
                    end = i;
                    tisover = true;
                }
            }
            if(tisover){
                pack.setTarget(ParseUtil.byteMerger(previous, ParseUtil.strChange(bytes, 0, end+1)));
                isover = true;
                if(end >= bytes.length-1)
                    pack.setReserve(null);
                pack.setReserve(ParseUtil.strChange(bytes, end+1, bytes.length));
                return pack;
            }else{
                previous = ParseUtil.byteMerger(previous, bytes);
                return null;
            }
        }
    }

    @Data
    private class SepPack{
        private byte[] target;
        private byte[] reserve;
    }

}

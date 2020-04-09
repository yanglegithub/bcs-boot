package com.phy.bcs.service.ifs.netty.codec.fep;

import com.phy.bcs.service.ifs.controller.model.ParseFEP;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ByteToFepMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 3) {
            return;
        }
        byte[] bytes = ByteBufUtil.getBytes(in);
        ParseFEP fep;
        try{
            fep = new ParseFEP(bytes);
        }catch (Exception e){
            return;
        }
        out.add(fep);
    }
}

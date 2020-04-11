package com.phy.bcs.service.ifs.netty.codec.fep;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.controller.model.ParseFEP;
import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ByteToFepMessageDecoder extends ByteToMessageDecoder {
    private byte flag = 0;
    private int length = 0;
    private int received= 0;
    BcsApplicationConfig config;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        config = config==null? SpringContextHolder.getBean(BcsApplicationConfig.class):config;
        if (in.readableBytes() < 1) {
            return;
        }
        if(flag == 0)
            flag = in.readByte();
        ByteBuf buf = null;
        switch (flag){
            case 0x31:
                if(in.readableBytes() < 68)
                    return;
                buf = in.readBytes(68);
                break;
            case 0x32:
                if(in.readableBytes() < 70)
                    return;
                buf = in.readBytes(70);
                break;
            case 0x33:
                if(in.readableBytes() < 2)
                    return;
                buf = in.readBytes(2);
                break;
            case 0x34:
                if(in.readableBytes() >= 6 + config.getPackgesize() && length - received >= config.getPackgesize()) {
                    buf = in.readBytes(6 + config.getPackgesize());
                    received += config.getPackgesize();
                }
                else if(in.readableBytes() == 6 + length - received && length - received < config.getPackgesize()) {
                    buf = in.readBytes(6 + length - received);
                    received = length;
                }
                else
                    return;
                break;
        }

        byte[] bytes = ParseUtil.byteMerger(new byte[]{flag}, ByteBufUtil.getBytes(buf));
        flag = 0;
        ParseFEP fep;
        try{
            fep = new ParseFEP(bytes);
        }catch (Exception e){
            return;
        }
        out.add(fep);
    }
}

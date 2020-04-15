package com.phy.bcs.service.ifs.netty.codec.fep;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.controller.model.ParseFEP;
import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ByteToFepMessageDecoder extends ByteToMessageDecoder {
    private byte flag = 0;
    private int length = 0;
    private int received= 0;
    BcsApplicationConfig config;
    ByteBuf temp = Unpooled.buffer();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        config = config==null? SpringContextHolder.getBean(BcsApplicationConfig.class):config;
        ByteBuf bufin = Unpooled.buffer();
        bufin.writeBytes(temp);
        bufin.writeBytes(in);
        if(flag == 0)
            flag = bufin.readByte();
        ByteBuf buf = null;
        switch (flag){
            case 0x01:
                if(bufin.readableBytes() < 68) {
                    temp.clear();
                    temp.writeBytes(bufin);
                    return;
                }
                buf = bufin.readBytes(68);
                break;
            case 0x02:
                if(bufin.readableBytes() < 70) {
                    temp.clear();
                    temp.writeBytes(bufin);
                    return;
                }
                buf = bufin.readBytes(70);
                break;
            case 0x03:
                if(bufin.readableBytes() < 2) {
                    temp.clear();
                    temp.writeBytes(bufin);
                    return;
                }
                buf = bufin.readBytes(2);
                break;
            case 0x04:
                if(bufin.readableBytes() >= 6 + config.getPackgesize() && length - received >= config.getPackgesize()) {
                    buf = bufin.readBytes(6 + config.getPackgesize());
                    received += config.getPackgesize();
                    temp.clear();
                    temp.writeBytes(bufin);
                }
                else if(bufin.readableBytes() >= 6 + length - received && length - received < config.getPackgesize()) {
                    buf = bufin.readBytes(6 + length - received);
                    received = length;
                    temp.clear();
                    temp.writeBytes(bufin);
                }
                else {
                    temp.clear();
                    temp.writeBytes(bufin);
                    return;
                }
                break;
                default:flag=0;return;
        }

        byte[] bytes = ParseUtil.byteMerger(new byte[]{flag}, ByteBufUtil.getBytes(buf));
        flag = 0;
        ParseFEP fep;
        try{
            fep = new ParseFEP(bytes);
        }catch (Exception e){
            return;
        }
        if(1 == fep.getFlag())
            length = fep.getSendFEPMode().getFileLength();
        out.add(fep);
    }
}

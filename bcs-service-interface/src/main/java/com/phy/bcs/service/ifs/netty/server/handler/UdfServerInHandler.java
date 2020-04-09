package com.phy.bcs.service.ifs.netty.server.handler;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.MyAppllicationConfig;
import com.phy.bcs.service.ifs.netty.codec.udf.UdfMessage;
import com.phy.bcs.service.ifs.netty.utils.NumberUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.math.BigInteger;

public class UdfServerInHandler extends ChannelInboundHandlerAdapter {
    private int count = 0;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /*System.out.println("==============server-read==============");
        System.out.println("the msg type is " + msg.getClass().getName());*/
        count++;
        System.out.println("count = " + count);
        UdfMessage message = UdfMessage.builder()
                .sid(NumberUtil.intToByte4(count))
                .did(NumberUtil.intToByte4(count))
                .mid(NumberUtil.unsignedShortToByte2(count))
                .bid(NumberUtil.intToByte4(count))
                .res(NumberUtil.intToByte4(count))
                .js(NumberUtil.intToByte4(count))
                .len(NumberUtil.unsignedShortToByte2(count))
                .data(NumberUtil.intToByte4(count))
                .build();
        ctx.writeAndFlush(message);
        //ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}

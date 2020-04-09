package com.phy.bcs.service.ifs.netty.client.handler;

import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PdxpClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        PdxpMessage message = PdxpMessage.builder().build();
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("==============client--read==============");
        System.out.println("the msg type is " + msg.getClass().getName());
        System.out.println(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

package com.phy.bcs.service.ifs.netty.client.handler;

import com.phy.bcs.service.ifs.netty.client.UdfClient;
import com.phy.bcs.service.ifs.netty.codec.udf.UdfMessage;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class UdfClientHandler extends ChannelInboundHandlerAdapter {
    private UdfMessage message;

    public UdfClientHandler(){
        super();
    }

    public UdfClientHandler(UdfMessage msg){
        this.message = msg;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(message != null)
            ctx.writeAndFlush(message);
        else
            ctx.writeAndFlush(Unpooled.copiedBuffer("11cdefghijklmnopqrstuvwxyz", CharsetUtil.UTF_8));
        ctx.close();
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

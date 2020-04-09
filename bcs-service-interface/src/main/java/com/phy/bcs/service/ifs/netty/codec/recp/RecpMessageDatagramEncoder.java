package com.phy.bcs.service.ifs.netty.codec.recp;

import com.phy.bcs.service.ifs.controller.model.ParseRECP;
import com.phy.bcs.service.ifs.controller.server.ParseModeToByte;
import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessage;
import com.phy.bcs.service.ifs.netty.utils.NumberUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;

public class RecpMessageDatagramEncoder extends MessageToMessageEncoder<ParseRECP> {
    private final InetSocketAddress remoteAddress;

    public RecpMessageDatagramEncoder(InetSocketAddress remoteAddress){
        this.remoteAddress = remoteAddress;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ParseRECP recp, List<Object> list) throws Exception {
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
        byte[] bytes = ParseModeToByte.parseRecpTo(recp);
        byteBuf.writeBytes(bytes);
        list.add(new DatagramPacket(byteBuf, this.remoteAddress));
    }
}

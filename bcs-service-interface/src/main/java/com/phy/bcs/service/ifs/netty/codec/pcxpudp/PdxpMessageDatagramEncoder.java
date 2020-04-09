package com.phy.bcs.service.ifs.netty.codec.pcxpudp;

import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessage;
import com.phy.bcs.service.ifs.netty.utils.NumberUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.List;

public class PdxpMessageDatagramEncoder extends MessageToMessageEncoder<PdxpMessage> {
    private final InetSocketAddress remoteAddress;

    public PdxpMessageDatagramEncoder(InetSocketAddress remoteAddress){
        this.remoteAddress = remoteAddress;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, PdxpMessage pdxp, List<Object> list) throws Exception {
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
        byteBuf.writeByte(pdxp.getVer());
        byteBuf.writeBytes(pdxp.getMid());
        byteBuf.writeBytes(pdxp.getSid());
        byteBuf.writeBytes(pdxp.getDid());
        byteBuf.writeBytes(pdxp.getBid());
        byteBuf.writeInt(pdxp.getNumber());
        byteBuf.writeByte(pdxp.getFlag());
        byteBuf.writeBytes(pdxp.getReserve());
        byteBuf.writeBytes(NumberUtil.unsignedShortToByte2(pdxp.getDate()));
        byteBuf.writeBytes(NumberUtil.intToByte4(pdxp.getTime()));
        byteBuf.writeBytes(NumberUtil.unsignedShortToByte2(pdxp.getL()));
        byteBuf.writeBytes(pdxp.getData());

        list.add(new DatagramPacket(byteBuf, this.remoteAddress));
    }
}

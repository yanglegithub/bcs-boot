package com.phy.bcs.service.ifs.netty.codec.fep;

import com.phy.bcs.service.ifs.controller.model.ParseFEP;
import com.phy.bcs.service.ifs.controller.server.ParseModeToByte;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class FepMessageToByteEncoder extends MessageToByteEncoder<ParseFEP> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ParseFEP msg, ByteBuf out) throws Exception {
        byte[] bytes = ParseModeToByte.parseFEPTo(msg);
        out.writeBytes(bytes);
    }
}

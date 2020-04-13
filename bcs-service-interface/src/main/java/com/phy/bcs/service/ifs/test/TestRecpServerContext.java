package com.phy.bcs.service.ifs.test;

import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.ifs.config.Constants;
import com.phy.bcs.service.ifs.controller.model.SendFEPMode;
import com.phy.bcs.service.ifs.netty.server.handler.RecpServerContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class TestRecpServerContext extends RecpServerContext {
    @Override
    public int sysTransfor(ChannelHandlerContext ctx, SendFEPMode fep) {
        return Constants.TSS_SYSTEM;
    }

    @Override
    public String protoTransfor(ChannelHandlerContext ctx, SendFEPMode fep) {
        return "RECP";
    }

    @Override
    public void filesend(InfFileStatus file) {
        System.out.println("---接收完毕，开始发送---");
        System.out.println("---接收完毕，发送完毕---");
    }
}


package com.phy.bcs.service.ifs.netty.server.handler.site26;

import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.ifs.config.Constants;
import com.phy.bcs.service.ifs.controller.model.SendFEPMode;
import com.phy.bcs.service.ifs.netty.client.RecpClient;
import com.phy.bcs.service.ifs.netty.client.handler.RecpClientHandler;
import com.phy.bcs.service.ifs.netty.server.handler.FepServerHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ManagerFepServerHandler extends FepServerHandler {
    @Override
    protected int sysTransfor(ChannelHandlerContext ctx, SendFEPMode mode) {
        return Constants.TSM_SYSTEM;
    }

    @Override
    protected String protoTransfor(ChannelHandlerContext ctx, SendFEPMode mode) {
        return "RECP";
    }

    @Override
    protected void fileSend(InfFileStatus file) {
        List<InfFileStatus> files = new ArrayList<>();
        files.add(file);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = getConfig().getTsmSystem().getIp();
                int port = getConfig().getTsmSystem().getFepPort();

                RecpClient client = new RecpClient(ip, port);
                RecpClientHandler handler = new RecpClientHandler(new InetSocketAddress(ip, port), files);
                try {
                    client.connect(handler);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

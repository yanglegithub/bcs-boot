package com.phy.bcs.service.ifs.netty.server.handler.site26;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.netty.client.PdxpUdpClient;
import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

public class ManagerPdxpTcpServerHandler extends SimpleChannelInboundHandler<PdxpMessage>{
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PdxpMessage msg) throws Exception {
        BcsApplicationConfig config = SpringContextHolder.getBean(BcsApplicationConfig.class);
        new Thread(new Runnable() {
            @Override
            public void run() {
                PdxpUdpClient client = new PdxpUdpClient(config.getTsmSystem().getIp(), config.getTsmSystem().getPdxpPort());
                try {
                    client.send(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        ctx.close();
    }
}

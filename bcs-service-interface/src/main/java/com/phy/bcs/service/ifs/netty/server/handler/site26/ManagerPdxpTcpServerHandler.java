package com.phy.bcs.service.ifs.netty.server.handler.site26;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.netty.client.PdxpUdpClient;
import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessage;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class ManagerPdxpTcpServerHandler extends SimpleChannelInboundHandler<PdxpMessage>{
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PdxpMessage msg) throws Exception {
        log.debug("Pdxp收到数据:{}", msg);
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

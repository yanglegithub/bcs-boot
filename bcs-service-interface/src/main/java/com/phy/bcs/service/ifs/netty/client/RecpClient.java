package com.phy.bcs.service.ifs.netty.client;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.file.service.InfFileStatusService;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.netty.client.handler.RecpClientHander;
import com.phy.bcs.service.ifs.netty.codec.recp.RecpMessageDatagramDecoder;
import com.phy.bcs.service.ifs.netty.codec.recp.RecpMessageDatagramEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.List;

public class RecpClient {
    private String host;
    private int port;
    BcsApplicationConfig config;

    public RecpClient(String host, int port){
        this.host = host;
        this.port = port;
        config = SpringContextHolder.getBean(BcsApplicationConfig.class);
    }

    public void connect(RecpClientHander handler) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true);
//            b.option(ChannelOption.AUTO_READ, true);
            b.handler(new ChannelInitializer<Channel>() {
                @Override
                public void initChannel(Channel ch) throws Exception {
                    ch.pipeline()
                            .addLast(new IdleStateHandler(config.getTimeout(), 0, 0))
                            .addLast(new RecpMessageDatagramDecoder())
                            .addLast(new RecpMessageDatagramEncoder(new InetSocketAddress(host, port)))
                            .addLast(handler);
                }
            });
            Channel ch = b.bind(0).sync().channel();

            ch.closeFuture().await();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void run() throws InterruptedException {
        InfFileStatusService service = SpringContextHolder.getBean(InfFileStatusService.class);
        List<InfFileStatus> files = service.findAllSendFiles();
        RecpClientHander handler = new RecpClientHander(new InetSocketAddress(host,port), files);
        connect(handler);
    }
}

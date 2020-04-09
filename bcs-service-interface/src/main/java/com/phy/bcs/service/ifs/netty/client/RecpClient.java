package com.phy.bcs.service.ifs.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

public class RecpClient {
    private String host;
    private int port;

    public RecpClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    public void connect(SimpleChannelInboundHandler<DatagramPacket> handler) throws InterruptedException {
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
                            .addLast(handler);
                }
            });
            Channel ch = b.bind(0).sync().channel();

            ch.closeFuture().await();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}

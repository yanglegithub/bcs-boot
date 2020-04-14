package com.phy.bcs.service.ifs.netty.client;

import com.phy.bcs.service.ifs.netty.codec.pcxpudp.PdxpMessageDatagramEncoder;
import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class PdxpUdpClient {
    private String host;
    private int port;

    public PdxpUdpClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    public void send(PdxpMessage msg) throws InterruptedException {
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
                    ch.pipeline().addLast(
                            new PdxpMessageDatagramEncoder(new InetSocketAddress(host, port)));
                }
            });
            Channel ch = b.bind(0).sync().channel();

            ch.writeAndFlush(msg);
            log.debug("pdxp发送数据:{}",msg);

            ch.closeFuture();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}

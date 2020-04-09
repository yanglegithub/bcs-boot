package com.phy.bcs.service.ifs.netty.client;

import com.phy.bcs.service.ifs.netty.client.handler.PdxpClientHandler;
import com.phy.bcs.service.ifs.netty.codec.pdxp.ByteToPdxpMessageDecoder;
import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessageToByteEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class PdxpClient {
    public void connect(String host, int port) throws Exception {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
//            b.option(ChannelOption.AUTO_READ, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new PdxpMessageToByteEncoder(),
                            new ByteToPdxpMessageDecoder(),
                            new PdxpClientHandler());
                }
            });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        PdxpClient client = new PdxpClient();
        client.connect("0.0.0.0", 12345);
    }
}

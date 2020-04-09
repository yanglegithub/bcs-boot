package com.phy.bcs.service.ifs.netty.client;

import com.phy.bcs.service.ifs.netty.client.handler.UdfClientHandler;
import com.phy.bcs.service.ifs.netty.codec.udf.ByteToUdfMessageDecoder;
import com.phy.bcs.service.ifs.netty.codec.udf.UdfMessage;
import com.phy.bcs.service.ifs.netty.codec.udf.UdfMessageToByteEncoder;
import com.phy.bcs.service.ifs.test.TestThread;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class UdfClient {
    private String host;
    private int port;

    public UdfClient(String host, int port){
        this.host = host;
        this.port = port;
    }

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
                            new UdfMessageToByteEncoder(),
                            new ByteToUdfMessageDecoder(),
                            new UdfClientHandler());
                }
            });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void connect(UdfMessage msg) throws Exception{
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
                            new UdfMessageToByteEncoder(),
                            new ByteToUdfMessageDecoder(),
                            new UdfClientHandler(msg));
                }
            });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) throws Exception {
        UdfClient client = new UdfClient("0.0.0.0", 12345);
        client.connect("0.0.0.0", 12345);
    }
}

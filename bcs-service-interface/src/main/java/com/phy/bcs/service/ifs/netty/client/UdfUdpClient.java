package com.phy.bcs.service.ifs.netty.client;

import com.phy.bcs.service.ifs.netty.codec.udf.UdfMessage;
import com.phy.bcs.service.ifs.netty.codec.udfUdp.UdfMessageDatagramEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

public class UdfUdpClient{
    private String host;
    private int port;

    public UdfUdpClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    public void send(UdfMessage msg) throws InterruptedException {
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
                            new UdfMessageDatagramEncoder(new InetSocketAddress(host, port)));
                }
            });
            Channel ch = b.bind(0).sync().channel();

            ch.writeAndFlush(msg);

            ch.closeFuture();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        UdfUdpClient client = new UdfUdpClient("127.0.0.1", 12345);
        UdfMessage msg = UdfMessage.builder()
                .sid(new byte[]{0x10,0x11,0x12,0x13})
                .did(new byte[]{0x20,0x21,0x22,0x23})
                .mid(new byte[]{0x30,0x31})
                .bid(new byte[]{0x40,0x41,0x42,0x43})
                .res(new byte[]{0x50,0x51,0x52,0x53})
                .js(new byte[]{0x60,0x61,0x62,0x63})
                .len(new byte[]{0x70,0x71})
                .data(new byte[]{(byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x88})
                .build();
        client.send(msg);
    }
}

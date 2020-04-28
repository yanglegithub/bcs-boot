package com.phy.bcs.service.ifs.netty.server;

import com.phy.bcs.service.ifs.netty.codec.pcxpudp.PdxpMessageDatagramDecoder;
import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class PdxpUdpServer extends Thread{
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap();
    private final int port;

    private SimpleChannelInboundHandler<DatagramPacket> handler;

    public PdxpUdpServer(int port, SimpleChannelInboundHandler<DatagramPacket> handler){
        this.port = port;
        this.handler = handler;
    }

    public void start(SimpleChannelInboundHandler<DatagramPacket> hander) throws InterruptedException {
        try{
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.SO_RCVBUF, 20 * 1024 * 1024)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(hander);
                        }
                    }).localAddress(port);
            ChannelFuture f = bootstrap.bind().sync();

            f.channel().closeFuture().sync();
        }finally {
            // 10. 关机的 EventLoopGroup，释放所有资源
            group.shutdownGracefully().sync();
        }
    }

    public void close(){
        group.shutdownGracefully();
    }

    @Override
    public void run() {
        try {
            start(handler);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

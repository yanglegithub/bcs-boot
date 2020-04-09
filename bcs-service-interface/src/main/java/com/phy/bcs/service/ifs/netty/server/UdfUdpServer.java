package com.phy.bcs.service.ifs.netty.server;

import com.phy.bcs.service.ifs.netty.codec.udf.UdfMessage;
import com.phy.bcs.service.ifs.netty.codec.udfUdp.UdfMessageDatagramDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UdfUdpServer {
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap();
    private final int port;

    public UdfUdpServer(int port){
        this.port = port;
    }

    public void start(SimpleChannelInboundHandler<UdfMessage> hander) throws InterruptedException {
        try{
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new UdfMessageDatagramDecoder());
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

    public static void main(String[] args) throws InterruptedException {
        UdfUdpServer server = new UdfUdpServer(12345);
        server.start(new SimpleChannelInboundHandler<UdfMessage>() {
            @Override
            protected void channelRead0(ChannelHandlerContext channelHandlerContext, UdfMessage udfMessage) throws Exception {
                System.out.println(udfMessage);
            }
        });
    }
}

package com.phy.bcs.service.ifs.netty.server;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.controller.model.SendFEPMode;
import com.phy.bcs.service.ifs.netty.server.handler.RecpServerContext;
import com.phy.bcs.service.ifs.netty.server.handler.RecpServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class RecpServer extends Thread{
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap();
    private final int port;
    BcsApplicationConfig config;

    public RecpServerHandler handler;

    public RecpServer(int port, RecpServerHandler handler){
        this.port = port;
        config = SpringContextHolder.getBean(BcsApplicationConfig.class);
        this.handler = handler;
    }

    public void start(RecpServerHandler hander) throws InterruptedException {
        try{
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(8192))
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new IdleStateHandler(config.getTimeout(), 0, 0));
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

    public static void main(String[] args) throws InterruptedException {/*
        RecpServer server = new RecpServer(12345);
        RecpServerHandler hander = new RecpServerHandler(new RecpServerContext() {
            @Override
            public int sysTransfor(ChannelHandlerContext ctx, SendFEPMode fep) {
                return 0;
            }

            @Override
            public String protoTransfor(ChannelHandlerContext ctx, SendFEPMode fep) {
                return "127.0.0.1";
            }

            @Override
            public void filesend(InfFileStatus file) {

            }
        }.getClass());
        server.start(hander);
    */}
}

package com.phy.bcs.service.ifs.netty.server;

import com.phy.bcs.service.ifs.controller.model.SendFEPMode;
import com.phy.bcs.service.ifs.netty.codec.pcxpudp.PdxpMessageDatagramDecoder;
import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessage;
import com.phy.bcs.service.ifs.netty.server.handler.RecpServerContext;
import com.phy.bcs.service.ifs.netty.server.handler.RecpServerHander;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class RecpServer {
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap();
    private final int port;

    public RecpServer(int port){
        this.port = port;
    }

    public void start(RecpServerHander hander) throws InterruptedException {
        try{
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
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

    public static void main(String[] args) throws InterruptedException {
        RecpServer server = new RecpServer(12345);
        RecpServerHander hander = new RecpServerHander(new RecpServerContext() {
            @Override
            public int sysTransfor(ChannelHandlerContext ctx, SendFEPMode fep) {
                return 0;
            }

            @Override
            public String protoTransfor(ChannelHandlerContext ctx, SendFEPMode fep) {
                return "127.0.0.1";
            }
        }.getClass());
        server.start(hander);
    }
}

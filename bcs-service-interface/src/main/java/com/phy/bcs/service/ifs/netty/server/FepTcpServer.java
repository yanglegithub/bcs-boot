package com.phy.bcs.service.ifs.netty.server;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.netty.codec.fep.ByteToFepMessageDecoder;
import com.phy.bcs.service.ifs.netty.codec.fep.FepMessageToByteEncoder;
import com.phy.bcs.service.ifs.netty.server.handler.FepOverTimeHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class FepTcpServer {
    private final int port;

    public FepTcpServer(int port) {
        this.port = port;
    }

    public void start(FepOverTimeHandler hander) throws InterruptedException {
        BcsApplicationConfig config = SpringContextHolder.getBean(BcsApplicationConfig.class);
        int timeout = config.getTimeout();

        // 3. 创建 EventLoopGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 4. 创建 ServerBootstrap
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    // 5. 指定使用 NIO 的传输 Channel
                    .channel(NioServerSocketChannel.class)
                    // 6. 设置 socket 地址使用所选的端口
                    .localAddress(port)
                    // 7. 添加 EchoServerHandler 到 Channel 的 ChannelPipeline
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new IdleStateHandler(0, 0, timeout),
                                            new ByteToFepMessageDecoder(),
                                            new FepMessageToByteEncoder(),
                                            hander);
                        }
                    });
            // 8. 绑定的服务器;sync 等待服务器关闭
            ChannelFuture f = b.bind().sync();
            System.out.println(UdfServer.class.getName() + " started and listen on " + f.channel().localAddress());
            // 9. 关闭 channel 和 块，直到它被关闭
            f.channel().closeFuture().sync();
        } finally {
            // 10. 关机的 EventLoopGroup，释放所有资源
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }
    }
}

package com.phy.bcs.service.ifs.netty.server;

import com.phy.bcs.service.ifs.netty.codec.pdxp.ByteToPdxpMessageDecoder;
import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessage;
import com.phy.bcs.service.ifs.netty.codec.pdxp.PdxpMessageToByteEncoder;
import com.phy.bcs.service.ifs.netty.server.handler.PdxpServerInHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PdxpServer extends Thread{
    private final int port;

    private final SimpleChannelInboundHandler<PdxpMessage> handler;

    public PdxpServer(int port, SimpleChannelInboundHandler<PdxpMessage> handler) {
        this.port = port;
        this.handler = handler;
    }

    public void start(SimpleChannelInboundHandler<PdxpMessage> handler) throws InterruptedException {
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
                    // 7. 添加 ServerHandler 到 Channel 的 ChannelPipeline
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new PdxpMessageToByteEncoder(),
                                            new ByteToPdxpMessageDecoder(),
                                            handler);
                        }
                    });
            // 8. 绑定的服务器;sync 等待服务器关闭
            ChannelFuture f = b.bind().sync();
            //System.out.println(PdxpServer.class.getName() + " started and listen on " + f.channel().localAddress());
            log.debug(" started and listen on " + f.channel().localAddress());
            // 9. 关闭 channel 和 块，直到它被关闭
            f.channel().closeFuture().sync();
        } finally {
            // 10. 关机的 EventLoopGroup，释放所有资源
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }
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
        if (args.length != 1) {
            System.err.println("Usage: " + PdxpServer.class.getSimpleName() + " <port>");
            return;
        }
        // 1. 设置端口值（抛出一个 NumberFormatException 如果该端口参数的格式不正确）
        int port = Integer.parseInt(args[0]);
        // 2. 呼叫服务器的 start() 方法
        new PdxpServer(port).start();
    */}
}

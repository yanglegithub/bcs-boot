package com.phy.bcs.service.ifs.netty.client;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.netty.client.handler.FepTcpClientHander;
import com.phy.bcs.service.ifs.netty.codec.fep.ByteToFepMessageDecoder;
import com.phy.bcs.service.ifs.netty.codec.fep.FepMessageToByteEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class FepTcpClient {

    public FepTcpClient(){
    }

    public void connect(String host, int port, String[] filepaths) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        BcsApplicationConfig config = SpringContextHolder.getBean(BcsApplicationConfig.class);
        int timeout = config.getTimeout();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
//            b.option(ChannelOption.AUTO_READ, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new IdleStateHandler(0, 0, timeout),
                            new FepMessageToByteEncoder(),
                            new ByteToFepMessageDecoder(),
                            new FepTcpClientHander(filepaths));
                }
            });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}

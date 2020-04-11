package com.phy.bcs.service.ifs.netty.client;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.file.service.InfFileStatusService;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.netty.client.handler.FepTcpClientHander;
import com.phy.bcs.service.ifs.netty.client.handler.FepTcpDbClientHander;
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

import java.util.List;
import java.util.Scanner;

public class FepTcpClient {

    public FepTcpClient(){
    }

    public void connect(String host, int port, List<InfFileStatus> filepaths) throws Exception {
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
                            new FepTcpDbClientHander(filepaths));
                }
            });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().await();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void run(){
        InfFileStatusService service = SpringContextHolder.getBean(InfFileStatusService.class);
        List<InfFileStatus> files = service.findAllSendFiles();
        try {
            Scanner scan = new Scanner(System.in);
            connect("192.168.1.104", 12345, files);
            System.out.println("---client close---");
            System.out.print("connect next:");
            while (true){
                int i = 0;
                if(scan.hasNextInt())
                    i = scan.nextInt();
                if (i == 0)
                    break;
                connect("192.168.1.104", 12345, files);
                System.out.println("---client close---");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

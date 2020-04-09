package com.phy.bcs.service.ifs.netty.server.handler;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.MyAppllicationConfig;
import com.phy.bcs.service.ifs.config.NetStatus;
import com.phy.bcs.service.ifs.netty.client.UdfClient;
import com.phy.bcs.service.ifs.netty.client.UdfUdpClient;
import com.phy.bcs.service.ifs.netty.codec.udf.UdfMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.math.BigInteger;

public class HZJUdfServerHander extends SimpleChannelInboundHandler<UdfMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, UdfMessage udfMessage) throws Exception {
        MyAppllicationConfig config  = SpringContextHolder.getBean(MyAppllicationConfig.class);
        int mid = new BigInteger(udfMessage.getMid()).intValue();
        boolean isFFOC = false;
        int[] sites = config.getMidip().getWssites();
        for (int i=0; i< sites.length; i++){
            if (mid == sites[i]){
                isFFOC = true;
                break;
            }
        }
        if(isFFOC){
            UdfUdpClient client = new UdfUdpClient(config.getIp().getFFOCIP(), config.getPort().getFFOCudf());
            client.send(udfMessage);
        } else {

            UdfClient tcp = new UdfClient(config.getIp().getTFCIP(), config.getPort().getTFCudf());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int reconnectTimes = config.getReconnectTimes();
                    boolean success = false;
                    while (!success){
                        try {
                            tcp.connect(udfMessage);
                            success = true;
                            NetStatus.writeStatus(0,true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            NetStatus.writeStatus(0, false);
                            System.out.println(Thread.currentThread().getName() + " + 1s");
                            reconnectTimes--;
                            if(reconnectTimes <= 0)
                                break;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }
    }
}

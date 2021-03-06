package com.phy.bcs.service.ifs.netty.server.handler;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.config.NetStatus;
import com.phy.bcs.service.ifs.netty.client.UdfClient;
import com.phy.bcs.service.ifs.netty.client.UdfUdpClient;
import com.phy.bcs.service.ifs.netty.codec.udf.UdfMessage;
import com.phy.bcs.service.ifs.netty.utils.NumberUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.math.BigInteger;

public class HZJUdfServerHander extends SimpleChannelInboundHandler<UdfMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, UdfMessage udfMessage) throws Exception {
        BcsApplicationConfig config  = SpringContextHolder.getBean(BcsApplicationConfig.class);
        String mid = String.valueOf(NumberUtil.byte2ToUnsignedShort(udfMessage.getMid()));
        boolean isFFOC = false;
        String[] sites = config.getWsmids();
        for (int i=0; i< sites.length; i++){
            if (mid.equals(sites[i])){
                isFFOC = true;
                break;
            }
        }
        if(isFFOC){
            UdfUdpClient client = new UdfUdpClient(config.getFfocSystem().getIp(), config.getFfocSystem().getUdfPort());
            client.send(udfMessage);
        } else {

            UdfClient tcp = new UdfClient(config.getTfcSystem().getIp(), config.getTfcSystem().getUdfPort());
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

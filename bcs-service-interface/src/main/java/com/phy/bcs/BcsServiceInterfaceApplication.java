package com.phy.bcs;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.MyAppllicationConfig;
import com.phy.bcs.service.ifs.netty.server.UdfUdpServer;
import com.phy.bcs.service.ifs.netty.server.handler.HZJUdfServerHander;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;

@SpringBootApplication
public class BcsServiceInterfaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BcsServiceInterfaceApplication.class, args);
        MyAppllicationConfig config = SpringContextHolder.getBean(MyAppllicationConfig.class);
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new UdfUdpServer(config.getPort().getLocalHzjUdf()).start(new HZJUdfServerHander());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
    }

}

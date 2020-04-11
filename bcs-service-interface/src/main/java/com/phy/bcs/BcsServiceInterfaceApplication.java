package com.phy.bcs;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.netty.client.FepTcpClient;
import com.phy.bcs.service.ifs.netty.client.RecpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BcsServiceInterfaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BcsServiceInterfaceApplication.class, args);
        BcsApplicationConfig config = SpringContextHolder.getBean(BcsApplicationConfig.class);
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new RecpClient("192.168.1.104", 12345).run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                FepTcpClient client = new FepTcpClient();
                client.run();
            }
        }).start();
    }

}

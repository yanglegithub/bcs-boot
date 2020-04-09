package com.phy.bcs;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
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
                    new UdfUdpServer(config.getPort().getLocalHzjUdf()).start(new HZJUdfServerHander());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
    }

}

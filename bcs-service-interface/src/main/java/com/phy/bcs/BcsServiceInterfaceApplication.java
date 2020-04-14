package com.phy.bcs;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.netty.client.FepTcpClient;
import com.phy.bcs.service.ifs.netty.client.RecpClient;
import com.phy.bcs.service.ifs.netty.server.*;
import com.phy.bcs.service.ifs.netty.server.handler.HZJUdfServerHander;
import com.phy.bcs.service.ifs.netty.server.handler.RecpServerHandler;
import com.phy.bcs.service.ifs.netty.server.handler.site26.*;
import com.phy.bcs.service.ifs.netty.server.handler.zj.ZjFepServerHandler;
import com.phy.bcs.service.ifs.netty.server.handler.zj.ZjRecpServerContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class BcsServiceInterfaceApplication {
    private static BcsApplicationConfig config;

    public static void main(String[] args) {
        SpringApplication.run(BcsServiceInterfaceApplication.class, args);
        config = SpringContextHolder.getBean(BcsApplicationConfig.class);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new UdfUdpServer(config.getLocalHzjUdfport()).start(new HZJUdfServerHander());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        startTSM();
        startTSS();
        startZJ();
    }

    public static void startTSS(){
        RecpServerHandler recphandler = new RecpServerHandler(SiteRecpServerContext.class);
        RecpServer tssRecpServer = new RecpServer(config.getLocalTssFepport(), recphandler);
        FepTcpServer tssFepServer = new FepTcpServer(config.getLocalTssFepport(), new SiteFepServerHandler());
        tssFepServer.start();
        tssRecpServer.start();
    }

    public static void startTSM(){
        RecpServerHandler recphandler = new RecpServerHandler(ManagerRecpServerContext.class);
        RecpServer tsmRecpServer = new RecpServer(config.getLocalTsmFepport(), recphandler);
        FepTcpServer tsmFepServer = new FepTcpServer(config.getLocalTsmFepport(), new ManagerFepServerHandler());
        PdxpServer pdxpTcpServer = new PdxpServer(config.getLocalTsmPdxpport(), new ManagerPdxpTcpServerHandler());
        PdxpUdpServer pdxpUdpServer = new PdxpUdpServer(config.getLocalTsmPdxpport(), new ManagerPdxpUdpServerHandler());
        tsmFepServer.start();
        tsmRecpServer.start();
        pdxpTcpServer.start();
        pdxpUdpServer.start();
    }

    public static void startZJ(){
        FepTcpServer fepTcpServer = new FepTcpServer(config.getLocalZjFepport(), new ZjFepServerHandler());
        RecpServer recpServer = new RecpServer(config.getLocalZjFepport(), new RecpServerHandler(ZjRecpServerContext.class));
        fepTcpServer.start();
        recpServer.start();
    }
}

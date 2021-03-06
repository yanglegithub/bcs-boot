package com.phy.bcs.service.ifs.netty.server.handler.zj;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.ifs.config.Constants;
import com.phy.bcs.service.ifs.controller.model.SendFEPMode;
import com.phy.bcs.service.ifs.ftp.config.FtpProperties;
import com.phy.bcs.service.ifs.netty.client.FepTcpClient;
import com.phy.bcs.service.ifs.netty.client.RecpClient;
import com.phy.bcs.service.ifs.netty.client.handler.RecpClientHandler;
import com.phy.bcs.service.ifs.netty.server.handler.RecpServerContext;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ZjRecpServerContext extends RecpServerContext {
    @Override
    public int sysTransfor(ChannelHandlerContext ctx, SendFEPMode fep) {
        String ip = getipv4FromRemoteAdress();
        int syscode = getConfig().getSyscodeByIp(ip);
        if(syscode == Constants.ZJ_SYSTEM){
            String filename = fep.getFileName().trim();
            String mid = filename.split("_")[1];
            int code = getConfig().getSystemCodeByMid(mid);
            return code!=-1?code:(Constants.TFC_SYSTEM);
        }else if(syscode == Constants.FFO_SYSTEM) {
            return Constants.ZJ_SYSTEM;
        }
        return 0-Constants.ZJ_SYSTEM;
    }

    @Override
    public String protoTransfor(ChannelHandlerContext ctx, SendFEPMode fep) {
        int code = sysTransfor(ctx, fep);
        if(code == Constants.TFC_SYSTEM)
            return "FTP";
        else
            return "RECP";
    }

    @Override
    public void filesend(InfFileStatus file) {
        FtpProperties ftpPro = SpringContextHolder.getBean(FtpProperties.class);
        List<InfFileStatus> files = new ArrayList<>();
        files.add(file);
        if (file.getToSystem() == Constants.TFC_SYSTEM) {
            String path = ftpPro.getSendLocalDir() + "/FTP";
            String newName = "TO5X_" + file.getFileName();
            file.setPath(path);
            file.saveFileInNewname(newName);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String ip = "";
                    int port = 0;
                    if (file.getToSystem() == Constants.ZJ_SYSTEM) {
                        ip = getConfig().getZjSystem().getIp();
                        port = getConfig().getZjSystem().getFepPort();
                    } else if (file.getToSystem() == Constants.FFO_SYSTEM) {
                        ip = getConfig().getFfocSystem().getIp();
                        port = getConfig().getFfocSystem().getFepPort();
                    }
                    RecpClient client = new RecpClient(ip, port);
                    RecpClientHandler handler = new RecpClientHandler(new InetSocketAddress(ip, port), files);
                    try {
                        client.connect(handler);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}

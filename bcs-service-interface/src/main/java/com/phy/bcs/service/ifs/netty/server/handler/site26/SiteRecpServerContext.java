package com.phy.bcs.service.ifs.netty.server.handler.site26;

import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.ifs.config.Constants;
import com.phy.bcs.service.ifs.controller.model.SendFEPMode;
import com.phy.bcs.service.ifs.netty.client.RecpClient;
import com.phy.bcs.service.ifs.netty.client.handler.RecpClientHandler;
import com.phy.bcs.service.ifs.netty.server.handler.RecpServerContext;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class SiteRecpServerContext extends RecpServerContext {
    @Override
    public int sysTransfor(ChannelHandlerContext ctx, SendFEPMode fep) {
        String ip = getRecpmode().getSourceAddress();
        int syscode = getConfig().getSyscodeByIp(ip);
        if(syscode == Constants.TSS_SYSTEM){
            String filename = fep.getFileName().trim();
            String mid = filename.split("_")[1];
            int code = getConfig().getSystemCodeByMid(Integer.parseInt(mid));
            return code!=-1?code:(Constants.TFC_SYSTEM);
        }else if(syscode == Constants.FFO_SYSTEM) {
            return Constants.TSS_SYSTEM;
        }
        return 0-Constants.TSS_SYSTEM;
    }

    @Override
    public String protoTransfor(ChannelHandlerContext ctx, SendFEPMode fep) {
        return "RECP";
    }

    @Override
    public void filesend(InfFileStatus file) {
        List<InfFileStatus> files = new ArrayList<>();
        files.add(file);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = "";
                int port = 0;
                if(file.getToSystem() == Constants.TSS_SYSTEM) {
                    ip = getConfig().getTssSystem().getIp();
                    port = getConfig().getTssSystem().getFepPort();
                }
                else if(file.getToSystem() == Constants.TFC_SYSTEM) {
                    ip = getConfig().getTfcSystem().getIp();
                    port = getConfig().getTfcSystem().getFepPort();
                }
                else if(file.getToSystem() == Constants.FFO_SYSTEM) {
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

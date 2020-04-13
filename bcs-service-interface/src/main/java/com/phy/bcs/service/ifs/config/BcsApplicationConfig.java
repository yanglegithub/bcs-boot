package com.phy.bcs.service.ifs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "net-application")
@Component
public class BcsApplicationConfig {
    @Data
    public static class IpPort{
        private String ip;
        private int udfPort;
        private int pdxpPort;
        private int fepPort;

    }
    private int[] wsmids;
    private int[] wxmids;
    private int netstatustimes;
    private int reconnectTimes;
    private int timeout;
    private int packgesize;

    private IpPort tfcSystem;
    private IpPort ffocSystem;
    private IpPort hzjSystem;
    private IpPort tssSystem;
    private IpPort tsmSystem;
    private IpPort zjSystem;

    private int localHzjUdfport;
    private int localTsmFepport;
    private int localTsmPdxpport;
    private int localTssFepport;
    private int localZjFepport;

    /**
     * 跟据ip找出对应的系统代码，若没有此ip，则返回-1
     * @param ipstr
     * @return
     */
    public int getSyscodeByIp(String ipstr){
        if(ipstr.equals(tfcSystem.getIp())){
            return Constants.TFC_SYSTEM;
        }else if(ipstr.equals(ffocSystem.getIp())){
            return Constants.FFO_SYSTEM;
        }else if(ipstr.equals(hzjSystem.getIp())){
            return Constants.HZJ_SYSTEM;
        }else if(ipstr.equals(zjSystem.getIp())){
            return Constants.ZJ_SYSTEM;
        }else if(ipstr.equals(tsmSystem.getIp())){
            return Constants.TSM_SYSTEM;
        }else if(ipstr.equals(tssSystem.getIp())){
            return Constants.TSS_SYSTEM;
        }
        return -1;
    }

    public String getIpBySystemcode(int code){
        if(code == Constants.TFC_SYSTEM){
            return tfcSystem.getIp();
        }else if(code == Constants.FFO_SYSTEM){
            return ffocSystem.getIp();
        }else if(code == Constants.HZJ_SYSTEM){
            return hzjSystem.getIp();
        }else if(code == Constants.ZJ_SYSTEM){
            return zjSystem.getIp();
        }else if(code == Constants.TSM_SYSTEM){
            return tsmSystem.getIp();
        }else if(code == Constants.TSS_SYSTEM){
            return tssSystem.getIp();
        }
        return "";
    }
    /**
     * 跟据mid找出该mid属于五型还是54系统
     * @param mid
     * @return
     */
    public int getSystemCodeByMid(int mid){
        int[] mids = wsmids;
        for (int i : mids){
            if(i == mid)
                return Constants.FFO_SYSTEM;
        }
        mids = wxmids;
        for (int i : mids){
            if(i == mid)
                return Constants.TFC_SYSTEM;
        }
        return -1;
    }
}

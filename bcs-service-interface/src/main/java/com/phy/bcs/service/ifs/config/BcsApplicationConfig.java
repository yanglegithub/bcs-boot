package com.phy.bcs.service.ifs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "my-application")
@Component
public class BcsApplicationConfig {
    private MidIp midip;
    private IP ip;
    private PORT port;

    private int netstatustimes;
    private int reconnectTimes;
    private int timeout;
    private int packgesize;

    /**
     * 跟据ip找出对应的系统代码，若没有此ip，则返回-1
     * @param ipstr
     * @return
     */
    public int getSyscodeByIp(String ipstr){
        if(ipstr.equals(ip.TFCIP)){
            return Constants.TFC_SYSTEM;
        }else if(ipstr.equals(ip.FFOCIP)){
            return Constants.FFO_SYSTEM;
        }else if(ipstr.equals(ip.HZJIP)){
            return Constants.HZJ_SYSTEM;
        }else if(ipstr.equals(ip.ZJIP)){
            return Constants.ZJ_SYSTEM;
        }else if(ipstr.equals(ip.TFMIP)){
            return Constants.TFM_SYSTEM;
        }else if(ipstr.equals(ip.TFSIP)){
            return Constants.TFS_SYSTEM;
        }
        return -1;
    }

    @Data
    public static class MidIp{
        /**
         * 五型对应任务号
         */
        private int[] wxsites;

        /**
         * 54对应任务号
         */
        private int[] wssites;
    }

    @Data
    public static class IP{
        //五型
        private String TFCIP;
        //54
        private String FFOCIP;
        //航侦局
        public String HZJIP;
        //中继
        public String ZJIP;
        //26长管
        public String TFMIP;
        //26站网
        public String TFSIP;
    }

    @Data
    public static class PORT{
        //五型udf协议端口
        private int TFCudf;
        //54udf协议端口
        private int FFOCudf;
        //本地航侦局模块udf协议端口
        private int localHzjUdf;
    }
}

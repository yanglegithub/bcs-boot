package com.phy.bcs.service.ifs.config;

import com.phy.bcs.common.util.spring.SpringContextHolder;
import org.springframework.stereotype.Component;

@Component
public class NetStatus {

    //HZJ网络状态记录
    private static byte[] HZJnetstatus;
    //五型任务控制状态记录
    private static byte[] TFCnetstatus;
    //54运控网络状态记录
    private static byte[] FFOCnetstatus;
    //中继网控中心状态记录
    private static byte[] ZJnetstatus;

    public static void init(){
        if(HZJnetstatus==null||TFCnetstatus==null||FFOCnetstatus==null||ZJnetstatus==null){
            BcsApplicationConfig config  = SpringContextHolder.getBean(BcsApplicationConfig.class);
            int timeConfig = config.getNetstatustimes();
            int bytes = 0;
            if(timeConfig > 0)
                bytes = timeConfig;
            else
                bytes = 1;
            HZJnetstatus = HZJnetstatus==null?new byte[bytes]:HZJnetstatus;
            TFCnetstatus = TFCnetstatus==null?new byte[bytes]:TFCnetstatus;
            FFOCnetstatus = FFOCnetstatus==null?new byte[bytes]:FFOCnetstatus;
            ZJnetstatus = ZJnetstatus==null?new byte[bytes]:ZJnetstatus;
        }
    }

    /**
     * 记录一次某个系统的网络状态
     * @param code 对象代号 如：0代表五型任务控制系统
     * @param isConnected 网络是否连通，true是，false否
     * @return -1 错误， 0 成功
     */
    public synchronized static int writeStatus(int code, boolean isConnected){
        init();
        byte[] target = null;
        switch (code){
            case 0: target = TFCnetstatus; break;
            case 1: target = FFOCnetstatus; break;
            case 2: target = HZJnetstatus; break;
            case 3: target = ZJnetstatus; break;
        }
        if(target == null)
            return -1;

        for (int i=0; i<target.length-1; i++){
            target[i+1] = target[i];
            if(i == 0)
                target[i] = (byte) (isConnected?0x01:0x00);
        }

        return 0;
    }

    /**
     * 连续N条消息转发都未收到回应判定为网络断开 返回-1
     * 连续N条消息转发有N-3条或N-3条以上收到回应判定为网络繁忙 返回0
     * 连续N条消息转发都有回应判定为网络正常显示为绿灯 返回1
     * 其它情况 返回2
     * @param code 对象代号 如：0代表五型任务控制系统
     * @return -2 错误, -1 网络断开, 0网络繁忙, 1网络通畅, 2其它情况
     */
    public int readStatus(int code){
        int cons = 0;
        int noncons = 0;
        byte[] target = null;
        switch (code){
            case 0: target = TFCnetstatus; break;
            case 1: target = FFOCnetstatus; break;
            case 2: target = HZJnetstatus; break;
            case 3: target = ZJnetstatus; break;
        }
        if(target == null)
            return -2;
        for(int i=0; i<target.length; i++){
            if(target[i] == 0x01)
                cons++;
            else
                noncons++;
        }
        if(cons == 0)
            return -1;
        else if(noncons == 0)
            return 1;
        else if(cons >= (target.length - 3))
            return 0;
        else
            return 2;
    }
}

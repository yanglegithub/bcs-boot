package com.phy.bcs.common.util;

import com.phy.bcs.common.constant.GlobalConstants;
import com.phy.bcs.common.util.rsa.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RequestUtils {
    protected static Logger log = LoggerFactory.getLogger(RequestUtils.class);

    public static String[] extractAndDecodeHeader(String header) throws IOException {

        byte[] base64Token = header.substring(6).getBytes("UTF-8");
        byte[] decoded;
        try {
            decoded = Base64.decode(base64Token);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to decode basic authentication token");
        }

        String token = new String(decoded, "UTF-8");

        int delim = token.indexOf(":");

        if (delim == -1) {
            throw new RuntimeException("Invalid basic authentication token");
        }
        return new String[]{token.substring(0, delim), token.substring(delim + 1)};
    }

    public static String getRemoteAddr(HttpServletRequest request) {
        String ipAddress = request.getHeader(GlobalConstants.X_REAL_IP);
        if (org.apache.commons.lang3.StringUtils.isEmpty(ipAddress) || GlobalConstants.UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader(GlobalConstants.X_FORWARDED_FOR);
        }
        if (org.apache.commons.lang3.StringUtils.isEmpty(ipAddress) || GlobalConstants.UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader(GlobalConstants.PROXY_CLIENT_IP);
        }
        if (org.apache.commons.lang3.StringUtils.isEmpty(ipAddress) || GlobalConstants.UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader(GlobalConstants.WL_PROXY_CLIENT_IP);
        }
        if (org.apache.commons.lang3.StringUtils.isEmpty(ipAddress) || GlobalConstants.UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader(GlobalConstants.HTTP_CLIENT_IP);
        }
        if (org.apache.commons.lang3.StringUtils.isEmpty(ipAddress) || GlobalConstants.UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader(GlobalConstants.HTTP_X_FORWARDED_FOR);
        }
        if (org.apache.commons.lang3.StringUtils.isEmpty(ipAddress) || GlobalConstants.UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (org.apache.commons.lang3.StringUtils.isEmpty(ipAddress) || GlobalConstants.UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (GlobalConstants.LOCALHOST_IP.equals(ipAddress) || GlobalConstants.LOCALHOST_IP_16.equals(ipAddress)) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    log.error("获取IP地址, 出现异常={}", e.getMessage(), e);
                }
                assert inet != null;
                ipAddress = inet.getHostAddress();
            }
            log.info("获取IP地址 ipAddress={}", ipAddress);
        }
        // 对于通过多个代理的情况, 第一个IP为客户端真实IP,多个IP按照','分割 //"***.***.***.***".length() = 15
        if (ipAddress != null && ipAddress.length() > GlobalConstants.MAX_IP_LENGTH) {
            if (ipAddress.indexOf(GlobalConstants.Symbol.COMMA) > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(GlobalConstants.Symbol.COMMA));
            }
        }
        return ipAddress;
    }

}

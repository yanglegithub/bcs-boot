package com.phy.bcs.service.ifs.ftp.config;

import lombok.Data;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("fileprocess.ftp")
@Configuration
@Data
public class FtpProperties extends GenericKeyedObjectPoolConfig {

    public static final String SYNC_SEND_PATH = "./.ftp-send-sync";
    public static final String SYNC_RETRY_SEND_PATH = "./.ftp-send-retry";

    private String host;
    private int port;
    private String username;
    private String password;
    private boolean passiveMode = true;

    /**
     * 读取远程文件时文件编码，取决于ftp上文件名称的文件编码
     */
    private String readerCharset = "UTF-8";
    /**
     * 发送本地文件时文件编码，取决于本地文件名称的文件编码
     */
    private String senderCharset = "UTF-8";

    private boolean readConcurrent = false;
    private boolean sendConcurrent = false;

    private String sendFilePattern;
    private String readFilePattern;
    private Long scanDelay = 5000L;
    /**
     * 待发送文件存放目录
     */
    private String sendLocalDir;
    /**
     * 已发送文件备份目录
     */
    private String sendBakLocalDir;
    /**
     * 读取mpss ftp文件暂存本地目录
     */
    private String readTempLocalDir;
    /**
     * 读取mpss ftp文件 处理完成后备份到的本地目录
     */
    private String readBakLocalDir;
    private boolean readerScannerEnable = false;
    private boolean senderScannerEnable = false;

}

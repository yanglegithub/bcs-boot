package com.phy.bcs.service.ifs.ftp.camel.process;

import com.phy.bcs.common.util.StringUtils;
import com.phy.bcs.service.ifs.ftp.camel.util.FileUtils;
import com.phy.bcs.service.ifs.ftp.config.FtpProperties;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.support.processor.idempotent.MemoryIdempotentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FtpSendRoute extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(FtpSendRoute.class);

    @Autowired
    private FtpProperties ftpProperties;
    @Autowired
    private FtpSendProcessor ftpSendProcessor;
    @Autowired
    private FtpSendFepProcessor ftpSendFepProcessor;

    private IdempotentRepository sendIdempotentRepository;

    public IdempotentRepository getSendIdempotentRepository() {
        return this.sendIdempotentRepository;
    }

    @Override
    public void configure() {
        seadFileRoute();
    }

    private void seadFileRoute() {
        String localDir = ftpProperties.getSendLocalDir();
        String localBakDir = ftpProperties.getSendBakLocalDir();
        String host = ftpProperties.getHost();
        int port = ftpProperties.getPort();
        String username = ftpProperties.getUsername();
        String password = ftpProperties.getPassword();
        FileUtils.initLocalDir(localDir);
        FileUtils.initLocalDir(localBakDir);
        FileUtils.initLocalDir(FtpProperties.SYNC_SEND_PATH);
        FileUtils.initLocalDir(FtpProperties.SYNC_RETRY_SEND_PATH);

        boolean scannerEnable = ftpProperties.isSenderScannerEnable();
        if (!scannerEnable) {
            log.warn("发送扫描未启动");
            return;
        }

        localBakDir = localBakDir.startsWith("./") ? "." + localBakDir : localBakDir;
        String filePattern = ftpProperties.getSendFilePattern();
        Integer delay = 1000;
        // 文件名编码格式，通过配置避免解析乱码
        String localCharset = ftpProperties.getSenderCharset();

        sendIdempotentRepository = MemoryIdempotentRepository.memoryIdempotentRepository(10000);
        getContext().getRegistry().bind("sendIdempotentRepository", sendIdempotentRepository);

        String fromLocalPathInfo = "file:" + localDir+ "/FTP" + "?delay=" + delay + "&charset=" + localCharset +
            "&move=" + localBakDir + "&readLockCheckInterval=1000" +
            "&idempotent=true&idempotentRepository=#bean:sendIdempotentRepository" +
            "&idempotentKey=${file:name}" +
            "&readLock=idempotent&readLockRemoveOnCommit=false";
        String fromLocalPathInfo_FEP = "file:" + localDir+ "/OTHER" + "?delay=" + delay + "&charset=" + localCharset +
                "&move=" + localBakDir + "&readLockCheckInterval=1000" +
                "&idempotent=true&idempotentRepository=#bean:sendIdempotentRepository" +
                "&idempotentKey=${file:name}" +
                "&readLock=idempotent&readLockRemoveOnCommit=false";

        if (StringUtils.isNotEmpty(filePattern)) {
            fromLocalPathInfo += "&include=" + filePattern;
        }
        //发送流水线
        from(fromLocalPathInfo)
            .process(ftpSendProcessor)
             .toD("ftp://"+host+":"+port+"/${in.header.nextUri}?username="+username+"&password="+password+"&fileName=${in.header.newFileName}")
            .log(LoggingLevel.INFO, logger, "Sender Process file ${file:name} complete.");
        //发送流水线
        from(fromLocalPathInfo_FEP)
                .process(ftpSendFepProcessor)
                .log(LoggingLevel.INFO, logger, "Sender Process file ${file:name} complete.");

        logger.debug("发送流水线已启动");
    }

}

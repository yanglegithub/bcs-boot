package com.phy.bcs.service.ifs.ftp.camel.process;

import com.phy.bcs.common.util.StringUtils;
import com.phy.bcs.service.ifs.ftp.camel.custom.CustomExceptionHandler;
import com.phy.bcs.service.ifs.ftp.camel.custom.CustomFtpClient;
import com.phy.bcs.service.ifs.ftp.camel.util.FileUtils;
import com.phy.bcs.service.ifs.ftp.config.FtpProperties;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileMessage;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.support.processor.idempotent.MemoryIdempotentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.RandomAccessFile;

@Component
public class FtpReadRoute extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(FtpReadRoute.class);

    @Autowired
    private FtpProperties ftpProperties;
    @Autowired
    private FtpReadProcessor ftpReadProcessor;

    @Override
    public void configure() {
        readFileRoute();
    }

    private void readFileRoute() {
        boolean scannerEnable = ftpProperties.isReaderScannerEnable();

        if (!scannerEnable) {
            log.warn("读取扫描未启动");
            return;
        }

        String host = ftpProperties.getHost();
        Integer port = ftpProperties.getPort();
        String username = ftpProperties.getUsername();
        String password = ftpProperties.getPassword();
        Long delay = ftpProperties.getScanDelay();
        String filePattern = ftpProperties.getReadFilePattern();
        String localTempFilePath = ftpProperties.getReadTempLocalDir();
        String localBakFilePath = ftpProperties.getReadBakLocalDir();
        String localSendDir = ftpProperties.getSendLocalDir();
        FileUtils.initLocalDir(localTempFilePath);
        FileUtils.initLocalDir(localSendDir);
        FileUtils.initLocalDir(localBakFilePath);
        localBakFilePath = localBakFilePath.startsWith("./") ? "." + localBakFilePath : localBakFilePath;

        boolean passiveMode1 = ftpProperties.isPassiveMode();

        // 文件名编码格式，通过配置避免解析乱码
        String localCharset = ftpProperties.getReaderCharset();
        //配置内存幂等仓库，处理过的文件会记录在内
        IdempotentRepository idempotentRepository = MemoryIdempotentRepository.memoryIdempotentRepository(10000);
        IdempotentRepository localIdempotentRepository = MemoryIdempotentRepository.memoryIdempotentRepository(10000);
        CustomExceptionHandler customExceptionHandler = new CustomExceptionHandler(idempotentRepository);
        getContext().getRegistry().bind("idempotentRepository", idempotentRepository);
        getContext().getRegistry().bind("localIdempotentRepository", localIdempotentRepository);
        getContext().getRegistry().bind("customExceptionHandler", customExceptionHandler);
        getContext().getRegistry().bind("customFtpClient", new CustomFtpClient(idempotentRepository));

        //读取FTP文件，下载到本地并删除 FTP 上文件
        /*String fromFtpServerInfo = "ftp://" + host + ":" + port + "?username=" + username + "&password=" + password +
            "&delay=" + delay + "&ftpClient.controlEncoding=" + localCharset + "&charset=" + localCharset +
            "&binary=true&passiveMode=" + passiveMode1 + "&ignoreFileNotFoundOrPermissionError=true" +
            "&streamDownload=true" +  //文件通过流式传输，避免遇到大文件内存溢出
            "&delete=true&readLockCheckInterval=100" +
            "&idempotent=true&idempotentRepository=#bean:idempotentRepository" +
            "&readLock=idempotent&readLockRemoveOnCommit=false" +
            "&ftpClient=#bean:customFtpClient" +
            "&onCompletionExceptionHandler=#bean:customExceptionHandler";*/
        String fromFtpServerInfo = "ftp://" + host + ":" + port;
        String ftpConfig = "?username=" + username + "&password=" + password +
                "&delay=" + delay + "&ftpClient.controlEncoding=" + localCharset + "&charset=" + localCharset +
                "&binary=true&passiveMode=" + passiveMode1 + "&ignoreFileNotFoundOrPermissionError=true" +
                "&streamDownload=true" +  //文件通过流式传输，避免遇到大文件内存溢出
                "&delete=true&readLockCheckInterval=100" +
                "&idempotent=true&idempotentRepository=#bean:idempotentRepository" +
                "&readLock=idempotent&readLockRemoveOnCommit=false" +
                "&ftpClient=#bean:customFtpClient" +
                "&onCompletionExceptionHandler=#bean:customExceptionHandler" +
                "&recursive=true" +
                "&filter=#readFileFilter";


        //读取本地收到的文件，处理并移动到备份目录
        String fromLocalPathInfo = "file:" + localTempFilePath + "?delay=" + delay +
            "&move=" + localBakFilePath + "&readLockCheckInterval=100" + "&idempotentKey=${file:name}" +
            "&idempotent=true&idempotentRepository=#bean:localIdempotentRepository" +
            "&readLock=idempotent&readLockRemoveOnCommit=false&readLockRemoveOnRollback=false";
        String fromHZJLocalPathInfo = "file:" + localTempFilePath + (localTempFilePath.endsWith("/")?"":"/") + "HZJ" + "?delay=" + delay +
                "&move=" + "../" + localBakFilePath + "&readLockCheckInterval=100" +
                "&idempotent=true&idempotentRepository=#bean:localIdempotentRepository" +
                "&readLock=idempotent&readLockRemoveOnCommit=false&readLockRemoveOnRollback=false";
        String fromInternalLocalPathInfo = "file:" + localTempFilePath + (localTempFilePath.endsWith("/")?"":"/") + "INTERNAL" + "?delay=" + delay +
                "&move=" + "../" + localBakFilePath + "&readLockCheckInterval=100" +
                "&idempotent=true&idempotentRepository=#bean:localIdempotentRepository" +
                "&readLock=idempotent&readLockRemoveOnCommit=false&readLockRemoveOnRollback=false";

        //添加文件名匹配规则
        if (StringUtils.isNotEmpty(filePattern)) {
            ftpConfig += "&include=" + filePattern;
            //fromLocalPathInfo += "&include=" + filePattern;
            fromHZJLocalPathInfo += "&include=" + filePattern;
            fromInternalLocalPathInfo += "&include=" + filePattern;
        }

        //读取FTP到本地
        from(fromFtpServerInfo + ftpConfig)
                .log(LoggingLevel.DEBUG, logger, "FTPREADER:read file or directory")
                .process(ftpReadProcessor)
                .toD("file:"+ localTempFilePath+"?fileName=${header.newFileName}")
                .log(LoggingLevel.INFO, logger, "Reader retrieve ftp file ${file:name} complete.");
        //读取HZJ发送的文件
        /*from(fromFtpServerInfo+"/HZJ/FROMHZJ"+ftpConfig)
                .to("file:"+localTempFilePath+(localTempFilePath.endsWith("/")?"":"/")+"HZJ/")
                .log(LoggingLevel.INFO, logger, "Reader retrieve ftp file ${file:name} complete.");*/
        //读取INTERNAL发送的文件
        /*from(fromFtpServerInfo+"/INTERNAL/FROMINTERNAL"+ftpConfig)
                .to("file:"+localTempFilePath+(localTempFilePath.endsWith("/")?"":"/")+"INTERNAL/")
                .log(LoggingLevel.INFO, logger, "Reader retrieve ftp file ${file:name} complete.");*/

        //处理本地收到的文件
        from(fromLocalPathInfo)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        GenericFileMessage<RandomAccessFile> inFileMessage = (GenericFileMessage<RandomAccessFile>) exchange.getIn();
                        GenericFile file = inFileMessage.getGenericFile();

                        String fileName = file.getFileName();
                        String filePath = file.getAbsoluteFilePath();
                        String sendpath = "OTHER";
                        if(fileName.startsWith("TOHZJ") || fileName.startsWith("TO5X")|| fileName.startsWith("TO54")){
                            sendpath = "FTP";
                        }
                        exchange.getMessage().setHeader("sendpath", sendpath);
                    }
                })
                .toD("file:" + localSendDir + "/${header.sendpath}")
                .log(LoggingLevel.INFO, logger, "Reader Process file ${file:name} complete.");
        //处理本地收到的HZJ文件
        /*from(fromHZJLocalPathInfo)
                .process(ftpReadProcessor)
                .toD("file:" + localSendDir + "?fileName=${in.header.newFileName}")
                .log(LoggingLevel.INFO, logger, "Reader Process file ${file:name} complete.");
        //处理本地收到的内部（54,5型）文件
        from(fromInternalLocalPathInfo)
                .process(ftpReadProcessor)
                .toD("file:" + localSendDir + "?fileName=${in.header.newFileName}")
                .log(LoggingLevel.INFO, logger, "Reader Process file ${file:name} complete.");*/

        logger.debug("读取流水线已启动");
    }
}

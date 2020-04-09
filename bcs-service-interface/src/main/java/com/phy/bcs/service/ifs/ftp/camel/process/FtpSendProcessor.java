package com.phy.bcs.service.ifs.ftp.camel.process;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

@Component
@Transactional(rollbackFor = Exception.class)
public class FtpSendProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(FtpSendProcessor.class);

    /**
     * 文件发送的处理逻辑
     * @param exchange
     */
    @Override
    public void process(Exchange exchange) {
        GenericFileMessage<RandomAccessFile> inFileMessage = (GenericFileMessage<RandomAccessFile>) exchange.getIn();
        GenericFile file = inFileMessage.getGenericFile();

        String fileName = file.getFileName();
        String filePath = file.getAbsoluteFilePath();
        logger.debug("fileName: " + fileName + "; filePath: " + filePath);
        File diskFile = new File(filePath);
        boolean sendResult = sendFile(diskFile);
        if (!sendResult) {
            //logger.error("文件发送失败！");
            //throw new RuntimeMsgException("文件发送失败！" + fileName);
        }
        String flag = fileName.split("_")[0];
        if ("TOHZJ".equals(flag)) {
            exchange.getMessage().getHeaders().put("nextUri", "HZJ/TOHZJ/");
            exchange.getMessage().getHeaders().put("newFileName", fileName.replaceFirst(flag + "_", ""));
        } else if ("TO5X".equals(flag)) {
            exchange.getMessage().getHeaders().put("nextUri", "INTERNAL/TO5X/");
            exchange.getMessage().getHeaders().put("newFileName", fileName.replaceFirst(flag + "_", ""));
        } else if ("TO54".equals(flag)) {
            exchange.getMessage().getHeaders().put("nextUri", "INTERNAL/TO54/");
            exchange.getMessage().getHeaders().put("newFileName", fileName.replaceFirst(flag + "_", ""));
        }
    }

    public boolean sendFile(File file) {
        if (!file.exists()) {
            logger.error("文件不存在，无法发送");
            return false;
        }
        String fileName = file.getName();
        String[] fileNameSplit = fileName.split("\\.")[0].split("_");


        return true;
    }

}


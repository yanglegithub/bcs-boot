package com.phy.bcs.service.ifs.ftp.camel.process;

import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.ftp.camel.filter.ReadFileFilter;
import com.phy.bcs.service.ifs.ftp.config.FtpProperties;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;

@Component
@Transactional(rollbackFor = Exception.class)
public class FtpReadProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(FtpReadProcessor.class);

    @Autowired
    private BcsApplicationConfig config;
    @Autowired
    private FtpProperties ftpProperties;

    /**
     * 文件读取到本地后的处理逻辑
     * @param exchange
     */
    @Override
    public void process(Exchange exchange) {
        GenericFileMessage<RandomAccessFile> inFileMessage = (GenericFileMessage<RandomAccessFile>) exchange.getIn();
        GenericFile file = inFileMessage.getGenericFile();

        String fileName = file.getFileName();
        String filePath = file.getAbsoluteFilePath();
        logger.debug("收到文件，文件名称: " + fileName);

        //文件转发
        String newName = fileName.substring(fileName.lastIndexOf('/') + 1);
        String handlepath = ReadFileFilter.pathHandle(filePath, false);
        if (handlepath.equals(ReadFileFilter.pathHandle(ftpProperties.getHzjSend(), true))) {
            String midstr = fileName.split("_")[2];
            int mid = Integer.parseInt(midstr, 16);
            boolean isWss = false;
            for (int m : config.getWsmids()){
                isWss = m==mid?true:false;
                if (isWss)
                    break;
            }
            if(isWss)
                newName = "TO54_" + newName;
            else
                newName = "TO5X_" +  newName;
        }
        else if (handlepath.equals(ReadFileFilter.pathHandle(ftpProperties.getInternalSendHzj(), true))) {
            newName = "TOHZJ_" + newName;
        }
        else if (handlepath.equals(ReadFileFilter.pathHandle(ftpProperties.getTfcSend26s(),true)))
            newName = "TO26S_" + newName;
        else if (handlepath.equals(ReadFileFilter.pathHandle(ftpProperties.getTfcSend26m(), true)))
            newName = "TO26M_" + newName;
        else if (handlepath.equals(ReadFileFilter.pathHandle(ftpProperties.getTfcSendZj(), true)))
            newName = "TOZJ_" + newName;

        exchange.getMessage().setHeader("newFileName", newName);
    }

    public static void main(String[] args) {
        String fileName = "MPSS_CCS_JB17-1_20191118_000001.GENDATA";
    }

}


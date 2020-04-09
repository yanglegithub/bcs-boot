package com.phy.bcs.service.ifs.ftp.camel.process;

import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
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
        File localFile = new File(filePath);

        //文件转发
        Map<String, Object> map = exchange.getMessage().getHeaders();
        if("HZJ".equals(localFile.getParentFile().getName())){
            String midstr = fileName.split("_")[1];
            int mid = Integer.parseInt(midstr);
            boolean isWss = false;
            for (int m : config.getMidip().getWssites()){
                isWss = m==mid?true:false;
                if (isWss)
                    break;
            }
            if(isWss)
                map.put("newFileName", "TO54_"+fileName);
            else
                map.put("newFileName", "TO5X_"+fileName);
        } else if("INTERNAL".equals(localFile.getParentFile().getName())){
            map.put("newFileName", "TOHZJ_"+fileName);
        }
    }

    public static void main(String[] args) {
        String fileName = "MPSS_CCS_JB17-1_20191118_000001.GENDATA";
    }

}


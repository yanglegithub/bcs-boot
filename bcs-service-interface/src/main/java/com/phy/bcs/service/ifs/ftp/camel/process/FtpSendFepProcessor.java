package com.phy.bcs.service.ifs.ftp.camel.process;

import com.jcraft.jsch.IO;
import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.ifs.config.BcsApplicationConfig;
import com.phy.bcs.service.ifs.config.Constants;
import com.phy.bcs.service.ifs.ftp.config.FtpProperties;
import com.phy.bcs.service.ifs.netty.client.RecpClient;
import com.phy.bcs.service.ifs.netty.client.handler.RecpClientHandler;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;

@Component
@Transactional(rollbackFor = Exception.class)
public class FtpSendFepProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(FtpSendFepProcessor.class);

    @Autowired
    private FtpProperties ftpProperties;
    @Autowired
    private BcsApplicationConfig config;
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
            logger.error("文件发送失败！");
            //throw new RuntimeMsgException("文件发送失败！" + fileName);
        }
    }

    public boolean sendFile(File file) {
        if (!file.exists()) {
            logger.error("文件不存在，无法发送");
            return false;
        }
        String fileName = file.getName();
        FileInputStream in = null;
        InfFileStatus infFile = new InfFileStatus();
        infFile.setFileName(fileName);
        infFile.setSendFinish(0);
        infFile.setRecFinish(1);
        infFile.setTransTimes(0);
        infFile.setFromProto("FTP");
        infFile.setToProto("RECP");
        infFile.setFromSystem(Constants.TFC_SYSTEM);
        try {
            in = new FileInputStream(file);
            byte[] content = new byte[in.available()];
            in.read(content);
            infFile.setLength(content.length);
            infFile.setFileContent(content);
            infFile.setCreateTime(new Date());
            infFile.setId(0);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }finally {
            try{
                if(in != null)
                    in.close();
            }catch (IOException e){
                return false;
            }
        }
        //String[] fileNameSplit = fileName.split("\\.")[0].split("_");
        final String ip;
        final int port;
        if(fileName.startsWith("TO26M")){
            infFile.setToSystem(Constants.TSM_SYSTEM);
            ip = config.getTsmSystem().getIp();
            port = config.getTsmSystem().getFepPort();
        }else if(fileName.startsWith("TOZJ")){
            infFile.setToSystem(Constants.ZJ_SYSTEM);
            ip = config.getZjSystem().getIp();
            port = config.getZjSystem().getFepPort();
        }else if(fileName.startsWith("TO26S")){
            infFile.setToSystem(Constants.TSS_SYSTEM);
            ip = config.getTssSystem().getIp();
            port = config.getTssSystem().getFepPort();
        }else
            return false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<InfFileStatus> files = new ArrayList<>();
                files.add(infFile);
                RecpClient client = new RecpClient(ip, port);
                RecpClientHandler handler = new RecpClientHandler(new InetSocketAddress(ip, port), files);
                try {
                    client.connect(handler);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return true;
    }

}


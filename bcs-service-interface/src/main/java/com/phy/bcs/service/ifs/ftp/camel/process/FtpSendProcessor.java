package com.phy.bcs.service.ifs.ftp.camel.process;

import com.phy.bcs.service.ifs.ftp.camel.filter.ReadFileFilter;
import com.phy.bcs.service.ifs.ftp.camel.util.FtpTool;
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

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
@Transactional(rollbackFor = Exception.class)
public class FtpSendProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(FtpSendProcessor.class);

    @Autowired
    private FtpProperties ftpProperties;
    private Map<String, FtpTool> ftpmap = new HashMap<>();
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
        if (!diskFile.exists()) {
            logger.error("文件不存在，无法发送");
        }
        String flag = fileName.split("_")[0];
        if ("TOHZJ".equals(flag)) {
            String servername = fileName.split("_")[2];
            FtpProperties.Ftpserver server = ftpProperties.findByServername(servername);
            /*exchange.getMessage().getHeaders().put("username", server.getUsername());
            exchange.getMessage().getHeaders().put("password", server.getPassword());
            exchange.getMessage().getHeaders().put("host", server.getHost());
            exchange.getMessage().getHeaders().put("port", server.getPort());
            exchange.getMessage().getHeaders().put("path", ReadFileFilter.pathHandle(ftpProperties.getHzjRead(), true));
            exchange.getMessage().getHeaders().put("newFileName", fileName.replaceFirst(flag + "_", ""));*/
            sendFile(diskFile,
                    fileName.replaceFirst(flag + "_", ""),
                    ReadFileFilter.pathHandle(ftpProperties.getHzjRead(), true),
                    server);
        } else if ("TO5X".equals(flag)) {
            FtpProperties.Ftpserver server = ftpProperties.getTfcFtpserver();
            /*exchange.getMessage().getHeaders().put("username", server.getUsername());
            exchange.getMessage().getHeaders().put("password", server.getPassword());
            exchange.getMessage().getHeaders().put("host", server.getHost());
            exchange.getMessage().getHeaders().put("port", server.getPort());
            exchange.getMessage().getHeaders().put("path", ReadFileFilter.pathHandle(ftpProperties.getTfcRead(), true));
            exchange.getMessage().getHeaders().put("newFileName", fileName.replaceFirst(flag + "_", ""));*/
            sendFile(diskFile,
                    fileName.replaceFirst(flag + "_", ""),
                    ReadFileFilter.pathHandle(ftpProperties.getTfcRead(), true),
                    server);
        } else if ("TO54".equals(flag)) {
            FtpProperties.Ftpserver server = ftpProperties.getFfocFtpserver();
            /*exchange.getMessage().getHeaders().put("username", server.getUsername());
            exchange.getMessage().getHeaders().put("password", server.getPassword());
            exchange.getMessage().getHeaders().put("host", server.getHost());
            exchange.getMessage().getHeaders().put("port", server.getPort());
            exchange.getMessage().getHeaders().put("path", ReadFileFilter.pathHandle(ftpProperties.getFfocRead(), true));
            exchange.getMessage().getHeaders().put("newFileName", fileName.replaceFirst(flag + "_", ""));*/
            sendFile(diskFile,
                    fileName.replaceFirst(flag + "_", ""),
                    ReadFileFilter.pathHandle(ftpProperties.getFfocRead(), true),
                    server);
        }
    }

    public boolean sendFile(File file, String newName, String path, FtpProperties.Ftpserver server) {
        if (!file.exists()) {
            logger.error("文件不存在，无法发送");
            return false;
        }
        String fileName = file.getName();
        String[] fileNameSplit = fileName.split("\\.")[0].split("_");
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FtpTool tool = getFtpTool(server);
        boolean success = tool.uploadFile("/"+path, newName, in);
        if(in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public FtpTool getFtpTool(FtpProperties.Ftpserver config){
        String id = config.getServername() + "_" + config.getUsername();
        if(ftpmap.get(id)==null){
            FtpTool tool = new FtpTool(config.getHost(), config.getPort(), config.getUsername(), config.getPassword());
            ftpmap.put(id, tool);
            return tool;
        }else {
            return ftpmap.get(id);
        }
    }

}


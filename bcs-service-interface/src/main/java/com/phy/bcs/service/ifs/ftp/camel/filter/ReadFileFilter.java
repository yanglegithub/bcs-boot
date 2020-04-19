package com.phy.bcs.service.ifs.ftp.camel.filter;


import com.phy.bcs.service.ifs.ftp.config.FtpProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@Slf4j
public class ReadFileFilter implements GenericFileFilter {
    @Autowired
    private FtpProperties ftpProperties;
    @Override
    public boolean accept(GenericFile file) {
        String filepath = file.getAbsoluteFilePath();
        filepath = pathHandle(filepath);
        if(filepath.equals(pathHandle(ftpProperties.getHzjSend()))
                ||filepath.equals(pathHandle(ftpProperties.getInternalSendHzj()))
                ||filepath.equals(pathHandle(ftpProperties.getTfcSend26m()))
                ||filepath.equals(pathHandle(ftpProperties.getTfcSend26s()))
                ||filepath.equals(pathHandle(ftpProperties.getTfcSendZj()))
                ||file.isDirectory()) {
            return true;
        }
        else
            return false;
    }

     public String pathHandle(String path){
        String handledpath = path.substring(0, path.lastIndexOf('/'));
        handledpath = path.replaceAll("/+","/");
        handledpath = handledpath.replaceAll("(^/+)|(/+$)","");
        return handledpath;
    }

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        System.out.printf("input:");
        while (scanner.hasNextLine()){
            String filename="abc/def/hijk/lmn.txt";
            String line = scanner.nextLine();
            line = line.substring(0, line.lastIndexOf('/'));
            line = line.replaceAll("/+","/");
            line = line.replaceAll("(^/+)|(/+$)","");
            System.out.println("handled:"+line);
            System.out.printf("input:");
        }
    }
}

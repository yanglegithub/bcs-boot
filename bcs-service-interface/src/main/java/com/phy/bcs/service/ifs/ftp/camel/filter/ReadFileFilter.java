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
        filepath = pathHandle(filepath, false);
        if(filepath.equals(pathHandle(ftpProperties.getHzjSend(), true))
                ||filepath.equals(pathHandle(ftpProperties.getInternalSendHzj(), true))
                ||filepath.equals(pathHandle(ftpProperties.getTfcSend26m() ,true))
                ||filepath.equals(pathHandle(ftpProperties.getTfcSend26s(), true))
                ||filepath.equals(pathHandle(ftpProperties.getTfcSendZj(), true))
                ||file.isDirectory()) {
            return true;
        }
        else
            return false;
    }

    public static String pathHandle(String path, boolean isDirectory){
        String handledpath;
        if(!isDirectory) {
            handledpath = path.substring(0, path.lastIndexOf('/') < 0 ? path.length() : path.lastIndexOf('/'))
                    .replaceAll("/+", "/")
                    .replaceAll("(^/+)|(/+$)", "");
        }else{
            handledpath = path.replaceAll("/+", "/").replaceAll("(^/+)|(/+$)", "");
        }
        return handledpath;
    }

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        System.out.printf("input:");
        while (scanner.hasNextLine()){
            String filename="abc/def/hijk/lmn.txt";
            String line = scanner.nextLine();
            String liner = pathHandle(line, false);
            System.out.println("filepath handled:"+liner);
            liner = pathHandle(line, true);
            System.out.println("directorypath handled:"+liner);
            System.out.printf("input:");
        }
    }
}

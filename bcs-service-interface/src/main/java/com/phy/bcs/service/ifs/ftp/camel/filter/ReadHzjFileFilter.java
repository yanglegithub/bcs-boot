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
public class ReadHzjFileFilter implements GenericFileFilter {
    @Autowired
    private FtpProperties ftpProperties;
    @Override
    public boolean accept(GenericFile file) {
        String filepath = file.getAbsoluteFilePath();
        filepath = ReadFileFilter.pathHandle(filepath, false);
        if(filepath.equals(ReadFileFilter.pathHandle(ftpProperties.getHzjSend(), true)) ||file.isDirectory()) {
            return true;
        }
        else
            return false;
    }
}

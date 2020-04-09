package com.phy.bcs.service.ifs.ftp.camel.filter;


import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.springframework.stereotype.Component;

@Component
public class ReadFileFilter implements GenericFileFilter {
    @Override
    public boolean accept(GenericFile file) {
        String filepath = file.getAbsoluteFilePath();
        if(filepath.startsWith("HZJ/FROMHZJ")||filepath.startsWith("INTERNAL/FROMINTERNAL")||file.isDirectory())
            return true;
        else
            return false;
    }
}

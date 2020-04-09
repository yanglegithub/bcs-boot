package com.phy.bcs.service.ifs.ftp.camel.custom;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class CustomFtpClient extends FTPClient {
    private IdempotentRepository idempotentRepository;

    public CustomFtpClient(IdempotentRepository idempotentRepository) {
        this.idempotentRepository = idempotentRepository;
    }

    @Override
    public InputStream retrieveFileStream(String remote) {
        try {
            return super.retrieveFileStream(remote);
        } catch (IOException e) {
            log.debug("下载失败，待重试。" + remote);
        }
        return null;
    }

    @Override
    public boolean retrieveFile(String remote, OutputStream local) {
        try {
            boolean result = super.retrieveFile(remote, local);
            if (!result) {
                this.idempotentRepository.remove(remote);
            }
            return result;
        } catch (Exception e) {
            log.debug("下载失败，待重试。" + remote);
        }
        return false;
    }

}

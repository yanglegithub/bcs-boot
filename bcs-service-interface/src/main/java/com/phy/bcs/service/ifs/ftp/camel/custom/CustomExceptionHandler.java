package com.phy.bcs.service.ifs.ftp.camel.custom;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileMessage;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.spi.ExceptionHandler;
import org.apache.camel.spi.IdempotentRepository;

import java.io.RandomAccessFile;

@Slf4j
public class CustomExceptionHandler implements ExceptionHandler {
    private IdempotentRepository idempotentRepository;

    public CustomExceptionHandler(IdempotentRepository idempotentRepository) {
        this.idempotentRepository = idempotentRepository;
    }

    @Override
    public void handleException(Throwable exception) {
        this.handleException((String) null, (Exchange) null, exception);
    }

    @Override
    public void handleException(String message, Throwable exception) {
        this.handleException(message, (Exchange) null, exception);
    }

    @Override
    public void handleException(String message, Exchange exchange, Throwable exception) {
        try {
            if (exception instanceof GenericFileOperationFailedException) {
                GenericFileMessage<RandomAccessFile> inFileMessage = (GenericFileMessage<RandomAccessFile>) exchange.getIn();
                GenericFile file = inFileMessage.getGenericFile();
                this.idempotentRepository.remove(file.getFileName());
                log.error("文件处理错误:" + file.getFileName());
            }
        } catch (Throwable var5) {
        }
    }
}

package com.phy.bcs.common.rest;

import com.phy.bcs.common.util.exception.RuntimeInfoException;
import com.phy.bcs.common.util.exception.RuntimeMsgException;
import com.phy.bcs.common.util.exception.RuntimeWarnException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * 异常统一处理
 * @author lijie
 */
@ControllerAdvice
@Slf4j
public class ExceptionTranslator {
    @ExceptionHandler({Exception.class})
    public ResponseEntity bindException(Exception e, HttpServletRequest request) throws Exception {
        log.warn("请求异常。链接: " + request.getRequestURI() + " , 异常原因: " + e.getMessage());
        CustomMessage message = new CustomMessage();
        message.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        message.setStatusEmun(CustomMessage.StatusEmun.MSG_TYPE_ERROR);
        if (e instanceof RuntimeMsgException) {
            RuntimeMsgException msg = (RuntimeMsgException) e;
            message.setStatusEmun(CustomMessage.StatusEmun.MSG_TYPE_ERROR);
            message.setData(msg.getData());
            message.addMessage(msg.getMessage());
        } else if (e instanceof RuntimeInfoException) {
            message.setHttpStatus(HttpStatus.OK);
            RuntimeMsgException msg = (RuntimeMsgException) e;
            message.setStatusEmun(CustomMessage.StatusEmun.MSG_TYPE_INFO);
            message.setData(msg.getData());
            message.addMessage(msg.getMessage());
        } else if (e instanceof RuntimeWarnException) {
            message.setHttpStatus(HttpStatus.OK);
            RuntimeMsgException msg = (RuntimeMsgException) e;
            message.setStatusEmun(CustomMessage.StatusEmun.MSG_TYPE_WARNING);
            message.setData(msg.getData());
            message.addMessage(msg.getMessage());
        } else {
            message.addMessage("操作异常: ");
            message.addMessage(e.getMessage());
            e.printStackTrace();
        }

        return ResultBuilder.build(message);
    }

}

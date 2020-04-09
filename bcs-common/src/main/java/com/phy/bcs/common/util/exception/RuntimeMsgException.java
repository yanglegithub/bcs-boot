package com.phy.bcs.common.util.exception;

import com.phy.bcs.common.util.PublicUtils;
import org.springframework.http.HttpStatus;

/**
 * 用于给前端/调用方返回错误提示信息
 */
public class RuntimeMsgException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String url;

    private HttpStatus code;
    private Object data;


    public RuntimeMsgException() {
        super();
    }

    public RuntimeMsgException(Object data, String... message) {
        super(PublicUtils.toAppendStr(message));
        this.setData(data);
    }

    public RuntimeMsgException(String... message) {
        super(PublicUtils.toAppendStr(message));
    }

    public RuntimeMsgException(String message, Object data) {
        super(message);
        this.setData(data);
    }

    public RuntimeMsgException(HttpStatus code, String message) {
        super(message);
        this.setCode(code);
    }

    public RuntimeMsgException(String message) {
        super(message);
    }

    public RuntimeMsgException(String message, HttpStatus httpStatus) {
        super(message);
        this.code = httpStatus;
    }

    public RuntimeMsgException(String message, String url) {
        super(message);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isRedirect() {
        return PublicUtils.isNotEmpty(url) && url.contains("redirect:");
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public HttpStatus getCode() {
        return code;
    }

    public void setCode(HttpStatus code) {
        this.code = code;
    }
}

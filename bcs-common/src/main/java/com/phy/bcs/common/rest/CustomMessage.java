package com.phy.bcs.common.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;

/**
 * 基础控制器支持类
 */
@ToString
@JsonPropertyOrder({"status", "code", "message", "data"})
public class CustomMessage<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 消息状态 -1错误 0信息 1成功 2警告
     */
    private int status;
    private StatusEmun statusEmun;
    /**
     * http请求状态码
     */
    private HttpStatus httpStatus;
    private String[] messages = {};
    private T data;
    public CustomMessage() {
    }

    public CustomMessage(T data, StatusEmun statusEmun, String... messages) {
        this.setStatusEmun(statusEmun);
        this.messages = messages;
        this.data = data;
    }

    public static <T> CustomMessage create(T data, StatusEmun statusEmunEmun, String... message) {
        CustomMessage msgModel = new CustomMessage(data, statusEmunEmun, message);
        return msgModel;
    }

    public static <T> CustomMessage createSuccessData(T data, String... message) {
        return create(data, StatusEmun.MSG_TYPE_SUCCESS, message);
    }

    public static CustomMessage createSuccess(String... message) {
        return create(null, StatusEmun.MSG_TYPE_SUCCESS, message);
    }

    public static <T> CustomMessage createWarn(T data, String... message) {
        return create(data, StatusEmun.MSG_TYPE_WARNING, message);
    }

    public static CustomMessage createWarn(String... message) {
        return create(null, StatusEmun.MSG_TYPE_WARNING, message);
    }

    public static <T> CustomMessage createError(T data, String... messages) {
        return create(data, StatusEmun.MSG_TYPE_ERROR, messages);
    }

    public static CustomMessage createError(String... messages) {
        return create(null, StatusEmun.MSG_TYPE_ERROR, messages);
    }

    public String getMessage() {
        return readMessages();
    }

    public void setMessage(String message) {
        addMessage(message);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
        this.statusEmun = StatusEmun.valueOf(status);
    }

    public String readMessages() {
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append(message);
        }
        return sb.toString();
    }

    public void addMessage(String message) {
        this.messages = ObjectUtils.addObjectToArray(messages, message);
    }

    @JsonIgnore
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public CustomMessage setHttpStatus(HttpStatus status) {
        this.httpStatus = status;
        return this;
    }

    @JsonIgnore
    public StatusEmun getStatusEmun() {
        if (this.statusEmun == null) {
            this.statusEmun = StatusEmun.valueOf(status);
        }

        return this.statusEmun;
    }

    public void setStatusEmun(StatusEmun statusEmun) {
        this.statusEmun = statusEmun;
        this.status = statusEmun.getStatus();
    }

    @JsonIgnore
    public String getStatusName() {
        return this.statusEmun.name();
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public enum StatusEmun {
        /**
         * 接口返回消息类型枚举
         */
        MSG_TYPE_INFO(0),
        MSG_TYPE_SUCCESS(1),
        MSG_TYPE_WARNING(2),
        MSG_TYPE_ERROR(-1);

        private int status;

        StatusEmun(int status) {
            this.status = status;
        }

        public static StatusEmun valueOf(int value) {
            StatusEmun state = null;
            if (state == null) {
                for (StatusEmun enumObj : values()) {
                    if (value == enumObj.status) {
                        return enumObj;
                    }
                }
            }
            return state;
        }

        public int getStatus() {
            return status;
        }

    }

}

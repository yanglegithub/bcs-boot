package com.phy.bcs.common.mvc.controller;

import com.phy.bcs.common.util.DateUtils;
import com.phy.bcs.common.util.JacksonUtils;
import com.phy.bcs.common.util.exception.RuntimeMsgException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.Date;

@Slf4j
public class GeneralResource {

    /*** 返回消息状态头 type */
    public static final String MSG_TYPE = "status";
    /*** 返回消息内容头 msg */
    public static final String MSG = "message";
    /*** 返回消息内容头 msg */
    public static final String DATA = "data";
    protected static final String RESPONSE_JSON = "application/json;charset=UTF-8";
    /**
     * ThreadLocal确保高并发下每个请求的request，response都是独立的
     */
    private static ThreadLocal<ServletRequest> currentRequest = new ThreadLocal<ServletRequest>();
    private static ThreadLocal<ServletResponse> currentResponse = new ThreadLocal<ServletResponse>();
    protected final String ENCODING_UTF8 = "UTF-8";
    /**
     * 验证Bean实例对象
     */
    @Resource
    protected Validator validator;

    public static final void writeStringHttpResponse(String str, HttpServletResponse response) {
        if (str == null) {
            throw new RuntimeMsgException("WRITE_STRING_RESPONSE_NULL");
        }
        response.reset();
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(str);
        } catch (IOException e) {
            log.warn("", e);
            throw new RuntimeException(e);
        }
    }

    public static final void writeJsonHttpResponse(Object object, HttpServletResponse response) {
        if (object == null) {
            throw new RuntimeMsgException("WRITE_JSON_RESPONSE_NULL");
        }
        try {
            response.setContentType(RESPONSE_JSON);
            response.setCharacterEncoding("UTF-8");
            String str = object instanceof String ? (String) object : JacksonUtils.toJsonString(object);
            log.info("write {}", str);
            response.getWriter().write(str);
        } catch (IOException e) {
            log.warn("", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 线程安全初始化reque，respose对象
     *
     * @param request
     * @param response
     */
    @ModelAttribute
    public void initReqAndRep(HttpServletRequest request, HttpServletResponse response) {
        currentRequest.set(request);
        currentResponse.set(response);
    }

    /**
     * 线程安全
     *
     * @return
     */
    public HttpServletRequest request() {
        return (HttpServletRequest) currentRequest.get();
    }

    /**
     * 线程安全
     *
     * @return
     */
    public HttpServletResponse response() {
        return (HttpServletResponse) currentResponse.get();
    }

    /**
     * 初始化数据绑定 1. 将所有传递进来的String进行HTML编码，防止XSS攻击 2. 将字段中Date类型转换为String类型
     */
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        // String类型转换，将所有传递进来的String进行HTML编码，防止XSS攻击
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {

            @Override
            public String getAsText() {
                Object value = getValue();
                return value != null ? value.toString() : "";
            }

            @Override
            public void setAsText(String text) {
                setValue(text == null ? null : StringEscapeUtils.escapeHtml4(text.trim()));
            }
        });
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });
    }
}

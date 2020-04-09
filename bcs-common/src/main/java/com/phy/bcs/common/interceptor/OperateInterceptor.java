package com.phy.bcs.common.interceptor;

import com.google.common.collect.Maps;
import com.phy.bcs.common.util.DateUtils;
import com.phy.bcs.common.util.RequestUtils;
import com.phy.bcs.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Map;

/**
 * 操作拦截器配置，用于生成每个请求的开始结束时间记录
 * @author lijie
 */
public class OperateInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<Long> START_TIME_THREAD_LOCAL = new NamedThreadLocal<Long>(
            "ThreadLocal StartTime");
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        boolean flag = false;
        // 1、开始时间
        long beginTime = System.currentTimeMillis();
        // 线程绑定变量
        START_TIME_THREAD_LOCAL.set(beginTime);
        flag = true;
        if (flag) {
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
            }
            if (logger.isDebugEnabled()) {
                logger.info("开始计时: {}  URI: {}",
                        new SimpleDateFormat("hh:mm:ss.SSS").format(beginTime),
                        request.getRequestURI());
            }
        } else {
            writeUrl(request, response, "/");
        }
        return flag;
    }

    public void writeUrl(HttpServletRequest request, HttpServletResponse response, String url)
            throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        StringBuffer path = new StringBuffer("<script> window.top.location.href='")
                .append(request.getScheme()).append("://").append(request.getServerName()).append(":")
                .append(request.getServerPort()).append(request.getContextPath());
        path.append(url).append("'</script>").toString();
        response.getWriter().println(path.toString());
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        // 得到线程绑定的局部变量（开始时间）
        long beginTime = START_TIME_THREAD_LOCAL.get();
        // 2、结束时间
        long endTime = System.currentTimeMillis();
        // 打印信息
        Map<String, String> params = Maps.newHashMap();
        Enumeration<String> keys = request.getParameterNames();
        String key;
        while (keys.hasMoreElements()) {
            key = keys.nextElement();
            params.put(key, StringUtils.abbr(
                    StringUtils.endsWithIgnoreCase(key, "password") ? "" : request.getParameter(key),
                    100));
        }
        logger.info("IP：{} 计时结束：{}  耗时：{}  URI: {} params: {} ", RequestUtils.getRemoteAddr(request),
                new SimpleDateFormat("hh:mm:ss.SSS").format(endTime),
                DateUtils.formatDateTime(endTime - beginTime), request.getRequestURI(), params);
    }
}

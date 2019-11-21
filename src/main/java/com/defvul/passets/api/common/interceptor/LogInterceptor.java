package com.defvul.passets.api.common.interceptor;

import com.defvul.passets.api.common.bean.LogBO;
import com.google.gson.Gson;
import io.swagger.annotations.ApiOperation;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LogInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LogInterceptor.class);
    private LogBO log = new LogBO();

    public LogInterceptor() {
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) {
        this.log.setRequestUrl(request.getRequestURI());
        this.log.setMethod(request.getMethod());
        this.log.setRemoteAddress(this.getIpAddr(request));
        this.log.setUserAgent(this.getUserAgent(request));
        this.log.setStartTime(new Date());
        this.log.setHeaders(this.getHeadersInfo(request));
        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView modelAndView) {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        this.log.setEndTime(new Date());
        this.log.setResponseStatus(response.getStatus());
        this.log.setResponseSize(response.getBufferSize());
        if (null != ex) {
            this.log.setException(ExceptionUtils.getStackTrace(ex));
        }

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod)handler;
            ApiOperation apiOperation = (ApiOperation)handlerMethod.getMethod().getAnnotation(ApiOperation.class);
            if (null != apiOperation) {
                this.log.setTitle(apiOperation.value());
                this.log.setContent(apiOperation.notes());
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug((new Gson()).toJson(this.log));
        }

    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    private String getIpAddr(HttpServletRequest request) {
        String unknown = "unknown";
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (StringUtils.isBlank(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (StringUtils.isBlank(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    private Map<String, String> getHeadersInfo(HttpServletRequest request) {
        Map<String, String> map = new HashMap();
        Enumeration headerNames = request.getHeaderNames();

        while(headerNames.hasMoreElements()) {
            String key = (String)headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }
}

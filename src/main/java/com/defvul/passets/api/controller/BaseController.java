package com.defvul.passets.api.controller;

import com.defvul.passets.api.common.bean.JwtPayload;
import com.defvul.passets.utils.JacksonJsonUtils;
import java.util.Base64;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class BaseController {
    public BaseController() {
    }

    protected Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    protected JwtPayload parseAuthToken() {
        ServletRequestAttributes context = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        return context == null ? null : getPayload(context.getRequest());
    }

    protected JwtPayload getPayload() {
        return this.parseAuthToken();
    }

    public static JwtPayload getPayload(HttpServletRequest request) {
        String authStr = request.getHeader("Authorization");
        if (authStr == null || authStr.trim().isEmpty()) {
            authStr = request.getParameter("access_token");
            if (authStr == null || authStr.trim().isEmpty()) {
                return null;
            }
        }

        String[] tokenPart = authStr.split("\\.");
        byte[] body = Base64.getDecoder().decode(tokenPart[1]);
        return (JwtPayload) JacksonJsonUtils.fromJsonStr(new String(body), JwtPayload.class);
    }
}

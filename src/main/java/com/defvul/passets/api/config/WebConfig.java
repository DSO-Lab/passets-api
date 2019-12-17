package com.defvul.passets.api.config;

import com.defvul.passets.api.interceptor.SecretInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 说明:
 * 时间: 2019/2/19 16:55
 *
 * @author wimas
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SecretInterceptor secretInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(secretInterceptor)
                .excludePathPatterns("/swagger**")
                .excludePathPatterns("/swagger-resources/**")
                .excludePathPatterns("/v2/**")
                .excludePathPatterns("/webjars/**")
                .addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**")
                .allowCredentials(true)
                .allowedMethods("DELETE", "OPTIONS", "PUT", "POST", "GET", "PATCH", "HEAD", "TRACE")
                .allowedOrigins("*")
                .allowedHeaders("*");
    }

}

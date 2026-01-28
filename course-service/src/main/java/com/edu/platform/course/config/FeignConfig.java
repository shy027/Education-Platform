package com.edu.platform.course.config;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign配置
 *
 * @author Education Platform
 */
@Configuration
public class FeignConfig {
    
    /**
     * Feign日志级别
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
    
    /**
     * Feign请求超时配置
     */
    @Bean
    public Request.Options options() {
        // 连接超时3秒，读超时5秒
        return new Request.Options(3, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, true);
    }
    
    /**
     * Feign请求拦截器，传递认证头
     */
    @Bean
    public feign.RequestInterceptor requestInterceptor() {
        return template -> {
            org.springframework.web.context.request.ServletRequestAttributes attributes = 
                (org.springframework.web.context.request.ServletRequestAttributes) 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
                String token = request.getHeader("Authorization");
                if (token != null) {
                    template.header("Authorization", token);
                }
            }
        };
    }
}

package com.edu.platform.community.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign配置类
 * 用于配置Feign客户端的请求拦截器
 */
@Slf4j
@Configuration
public class FeignConfig {

    /**
     * Feign请求拦截器
     * 将当前请求的认证信息传递给被调用的服务
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    
                    // 传递Authorization header
                    String authorization = request.getHeader("Authorization");
                    if (authorization != null) {
                        template.header("Authorization", authorization);
                        log.debug("Feign传递Authorization: {}", authorization.substring(0, Math.min(20, authorization.length())));
                    }
                    
                    // 传递用户ID和用户名(网关透传的header)
                    String userId = request.getHeader("X-User-Id");
                    if (userId != null) {
                        template.header("X-User-Id", userId);
                    }
                    
                    String username = request.getHeader("X-Username");
                    if (username != null) {
                        template.header("X-Username", username);
                    }
                }
            }
        };
    }
}

package com.edu.platform.course.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置
 * <p>
 * 课程服务的认证由 API 网关统一处理（JWT 过滤器），
 * 服务内部只需要支持方法级权限注解（@PreAuthorize 等），
 * 不再做 HTTP 层的 Token 校验。
 *
 * @author Education Platform
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（前后端分离，使用 JWT 无状态认证）
            .csrf(AbstractHttpConfigurer::disable)

            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // Knife4j / Swagger 文档端点全部放行
                .requestMatchers(
                    "/doc.html",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/webjars/**"
                ).permitAll()

                // 内部接口（Feign 服务间调用）放行
                .requestMatchers("/internal/**").permitAll()

                // 其他接口全部放行（由网关统一鉴权，此处不再重复校验）
                .anyRequest().permitAll()
            )

            // 无状态会话（JWT 不使用 Session）
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }
}

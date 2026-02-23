package com.edu.platform.resource.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.edu.platform.common.utils.JwtUtil;
import com.edu.platform.common.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息拦截器
 * <p>
 * 用户信息来源（优先级从高到低）：
 * 1. 网关透传的 Header（X-User-Id / X-Username / X-User-Roles）
 * 2. 请求中的 Authorization Bearer Token（直连调试场景）
 * <p>
 * 同时填充 UserContext（ThreadLocal）和 Spring Security SecurityContextHolder，
 * 确保 @PreAuthorize 权限注解能正确识别角色。
 */
@Slf4j
@Component
public class UserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 优先尝试从 Gateway 透传的 Header 获取
        String headerUserId = request.getHeader("X-User-Id");
        if (StrUtil.isNotBlank(headerUserId)) {
            try {
                Long userId = Long.parseLong(headerUserId);
                String username = request.getHeader("X-Username");
                String rolesJson = request.getHeader("X-User-Roles");

                UserContext.setUserId(userId);
                UserContext.setUsername(username);

                List<String> roles = Collections.emptyList();
                if (StrUtil.isNotBlank(rolesJson)) {
                    roles = JSON.parseObject(rolesJson, new TypeReference<List<String>>() {});
                    UserContext.setRoles(roles);
                }

                setSecurityContext(username, roles);
                return true;
            } catch (NumberFormatException e) {
                log.warn("Invalid X-User-Id header: {}", headerUserId);
            } catch (Exception e) {
                log.warn("Failed to parse user headers: {}", e.getMessage());
            }
        }

        // 2. 尝试解析 Authorization Header（本地调试或直连场景）
        String token = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(token)) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            if (JwtUtil.validateToken(token)) {
                Long userId = JwtUtil.getUserId(token);
                String username = JwtUtil.getUsername(token);
                List<String> roles = JwtUtil.getRoles(token);

                UserContext.setUserId(userId);
                UserContext.setUsername(username);
                UserContext.setRoles(roles);

                setSecurityContext(username, roles != null ? roles : Collections.emptyList());
                return true;
            }
        }

        // 3. 未获取到用户信息，放行
        return true;
    }

    private void setSecurityContext(String username, List<String> roles) {
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                .collect(Collectors.toList());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
        SecurityContextHolder.clearContext();
    }
}

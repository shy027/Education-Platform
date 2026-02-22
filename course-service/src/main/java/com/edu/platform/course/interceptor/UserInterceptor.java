package com.edu.platform.course.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.edu.platform.common.utils.JwtUtil;
import com.edu.platform.common.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * 用户信息拦截器
 * <p>
 * 用户信息来源（优先级从高到低）：
 * 1. 网关透传的 Header（X-User-Id / X-Username / X-User-Roles）
 * 2. 请求中的 Authorization Bearer Token（直连调试场景）
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
                UserContext.setUserId(Long.parseLong(headerUserId));
                UserContext.setUsername(request.getHeader("X-Username"));
                // 解析网关透传的角色列表（JSON 数组字符串，如 ["ROLE_TEACHER"]）
                String rolesJson = request.getHeader("X-User-Roles");
                if (StrUtil.isNotBlank(rolesJson)) {
                    List<String> roles = JSON.parseObject(rolesJson, new TypeReference<List<String>>() {});
                    UserContext.setRoles(roles);
                }
                return true;
            } catch (NumberFormatException e) {
                log.warn("Invalid X-User-Id header: {}", headerUserId);
            } catch (Exception e) {
                log.warn("Failed to parse X-User-Roles header: {}", e.getMessage());
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
                return true;
            }
        }

        // 3. 未获取到用户信息，放行（Context 为空）
        // 对于必须登录的接口，网关已拦截；直连场景由 Controller/Service 层校验
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}

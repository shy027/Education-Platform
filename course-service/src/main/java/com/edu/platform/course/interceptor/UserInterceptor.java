package com.edu.platform.course.interceptor;

import cn.hutool.core.util.StrUtil;
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
                // Header中传递角色列表可能比较复杂(JSON)，这里简化处理，如果能解析最好，否则暂为空
                // UserContext.setRoles(...); 
                return true;
            } catch (NumberFormatException e) {
                log.warn("Invalid X-User-Id header: {}", headerUserId);
            }
        }

        // 2. 尝试解析 Authorization Header (本地调试或直连场景)
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
        
        // 3. 未登录或无法识别用户，是否放行取决于业务。
        // 对于必须登录的接口，Controller层或Service层会校验 Context 中的 ID
        // 这里均放行，但 Context 为空
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}

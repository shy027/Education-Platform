package com.edu.platform.report.filter;

import com.edu.platform.common.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器
 * 从请求头中解析JWT token,验证并设置用户认证信息
 *
 * @author Education Platform
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 1. 从请求头获取token
        String token = getTokenFromRequest(request);
        
        // 2. 验证token并设置认证信息
        if (token != null && JwtUtil.validateToken(token)) {
            try {
                // 解析token获取用户信息
                Long userId = JwtUtil.getUserId(token);
                String username = JwtUtil.getUsername(token);
                List<String> roles = JwtUtil.getRoles(token);
                
                // 转换角色为Spring Security的Authority格式
                List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
                
                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                // 设置到Security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("JWT认证成功: userId={}, username={}, roles={}", userId, username, roles);
            } catch (Exception e) {
                log.error("JWT认证失败", e);
                SecurityContextHolder.clearContext();
            }
        }
        
        // 3. 继续过滤链
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求头中提取JWT token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

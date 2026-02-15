package com.edu.platform.audit.security;

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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器
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
        
        String requestURI = request.getRequestURI();
        log.debug("JWT过滤器处理请求: {}", requestURI);
        
        // 获取Token
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token)) {
            try {
                // 验证Token
                if (JwtUtil.validateToken(token)) {
                    // 获取用户名和角色
                    String username = JwtUtil.getUsername(token);
                    List<String> roles = JwtUtil.getRoles(token);
                    
                    log.debug("Token验证成功 - username: {}, roles: {}", username, roles);
                    
                    if (username != null) {
                        // 转换角色为权限
                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList());
                        
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                username, 
                                null, 
                                authorities
                            );
                        
                        // 设置到Security上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        log.debug("JWT认证成功: username={}, roles={}", username, roles);
                    }
                } else {
                    log.warn("Token验证失败");
                }
            } catch (Exception e) {
                log.error("JWT认证失败: {}", e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

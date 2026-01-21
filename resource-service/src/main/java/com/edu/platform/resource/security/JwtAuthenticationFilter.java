package com.edu.platform.resource.security;

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
        log.info("JWT过滤器处理请求: {}", requestURI);
        
        // 获取Token
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token)) {
            log.info("检测到Token: {}", token.substring(0, Math.min(20, token.length())) + "...");
            try {
                // 验证Token
                if (JwtUtil.validateToken(token)) {
                    // 获取用户名和角色
                    String username = JwtUtil.getUsername(token);
                    List<String> roles = JwtUtil.getRoles(token);
                    
                    log.info("Token验证成功 - username: {}, roles: {}", username, roles);
                    
                    if (username != null) {
                        // 转换角色为权限
                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList());
                        
                        log.info("设置权限: {}", authorities);
                        
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                username, 
                                null, 
                                authorities
                            );
                        
                        // 设置到Security上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        log.info("JWT认证成功: username={}, roles={}, authorities={}", username, roles, authorities);
                    } else {
                        log.warn("username为null");
                    }
                } else {
                    log.warn("Token验证失败");
                }
            } catch (Exception e) {
                log.error("JWT认证失败: {}", e.getMessage(), e);
            }
        } else {
            log.info("请求中没有Token");
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

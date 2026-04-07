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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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
                
                try {
                    // 设置 UserContext
                    com.edu.platform.common.utils.UserContext.setUserId(userId);
                    com.edu.platform.common.utils.UserContext.setUsername(username);
                    com.edu.platform.common.utils.UserContext.setRoles(roles);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.info("JWT认证成功: userId={}, username={}, roles={}, authorities={}", userId, username, roles, authorities);
                    
                    filterChain.doFilter(request, response);
                } finally {
                    // 清理 UserContext，防止线程复用导致的数据污染
                    com.edu.platform.common.utils.UserContext.clear();
                }
            } catch (Exception e) {
                log.error("JWT认证失败", e);
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
            }
        } else {
            // 3. 继续过滤链
            filterChain.doFilter(request, response);
        }
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

package com.edu.platform.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.edu.platform.gateway.config.GatewayJwtProperties;
import com.edu.platform.gateway.exception.TokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 认证全局过滤器
 * <p>
 * 执行顺序：
 * 1. 白名单路径直接放行
 * 2. 从 Authorization Header 取 Bearer Token
 * 3. 验证 Token 有效性（过期/无效分别抛出对应异常）
 * 4. 解析 userId、username、roles 注入下游 Header
 *
 * @author Education Platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    /**
     * JWT 密钥（与 platform-common 中的 JwtUtil 保持一致）
     */
    private static final String SECRET_KEY = "education-platform-secret-key-2026-very-long-string";

    /**
     * Token 前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 路径匹配器（支持 Ant 通配符，如 /api/**）
     */
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 网关自定义配置属性（注入白名单列表等）
     */
    private final GatewayJwtProperties gatewayProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. 白名单放行
        if (isWhiteListed(path)) {
            log.debug("白名单放行: {}", path);
            return chain.filter(exchange);
        }

        // 2. 获取 Token
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            log.warn("请求未携带Token: {}", path);
            return Mono.error(TokenException.missing());
        }

        String token = authHeader.substring(TOKEN_PREFIX.length());

        // 3. 解析并验证 Token
        Claims claims;
        try {
            claims = parseToken(token);
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", path);
            return Mono.error(TokenException.expired());
        } catch (Exception e) {
            log.warn("Token无效: {}, error: {}", path, e.getMessage());
            return Mono.error(TokenException.invalid());
        }

        // 4. 提取用户信息
        Long userId = Long.parseLong(claims.getSubject());
        String username = claims.get("username", String.class);
        List<String> roles = getRolesFromClaims(claims);

        log.debug("JWT认证通过: userId={}, username={}, path={}", userId, username, path);

        // 5. 将用户信息透传到下游 Header
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", username != null ? username : "")
                .header("X-User-Roles", JSON.toJSONString(roles))
                // 保留原始 Authorization Header，方便下游服务（如 user-service）继续验证
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 过滤器优先级（最高，先于其他过滤器执行）
     * Ordered.HIGHEST_PRECEDENCE = Integer.MIN_VALUE
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * 判断是否在白名单中（支持 Ant 通配符）
     */
    private boolean isWhiteListed(String path) {
        List<String> whiteList = gatewayProperties.getWhiteList();
        if (whiteList == null || whiteList.isEmpty()) {
            return false;
        }
        for (String pattern : whiteList) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析 JWT Token
     */
    private Claims parseToken(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Claims 中提取角色列表
     */
    @SuppressWarnings("unchecked")
    private List<String> getRolesFromClaims(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List) {
            return (List<String>) roles;
        }
        return List.of();
    }
}

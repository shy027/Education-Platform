package com.edu.platform.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 网关全局配置
 * - IP 级别限流 KeyResolver
 * - 默认 RedisRateLimiter（令牌桶：100 次/秒，峰值 200）
 *
 * @author Education Platform
 */
@Configuration
public class GatewayConfig {

    /**
     * 按客户端 IP 进行限流（X-Forwarded-For 优先）
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown";
            }
            // 取第一个 IP（代理链中最原始的客户端）
            String clientIp = ip.split(",")[0].trim();
            return Mono.just(clientIp);
        };
    }

    /**
     * 全局默认限流器：每秒 100 个令牌，桶容量 200
     * replenishRate: 令牌桶每秒填充速率
     * burstCapacity: 令牌桶最大容量（瞬时峰值）
     * requestedTokens: 每次请求消耗令牌数
     */
    @Bean
    public RedisRateLimiter defaultRedisRateLimiter() {
        return new RedisRateLimiter(100, 200, 1);
    }
}

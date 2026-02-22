package com.edu.platform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关服务启动类
 * <p>
 * 职责：
 * - 统一 API 入口（端口 8080）
 * - JWT 认证与用户信息透传
 * - 路由转发（基于 Nacos 服务发现 + 负载均衡）
 * - 跨域处理（CORS）
 * - 接口限流（Redis 令牌桶）
 * - API 文档聚合（Knife4j Gateway）
 *
 * @author Education Platform
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("=========================================");
        System.out.println("  网关服务启动成功! 端口: 8080");
        System.out.println("  文档聚合: http://localhost:8080/doc.html");
        System.out.println("=========================================");
    }
}

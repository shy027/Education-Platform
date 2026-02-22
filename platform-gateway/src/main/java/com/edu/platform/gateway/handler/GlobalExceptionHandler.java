package com.edu.platform.gateway.handler;

import com.alibaba.fastjson2.JSON;
import com.edu.platform.gateway.exception.TokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 网关全局异常处理器（WebFlux）
 * <p>
 * 优先级高于 Spring Boot 默认的 DefaultErrorWebExceptionHandler，
 * 统一将所有异常转换为标准 JSON 格式（与下游服务的 Result 结构一致）：
 * {"code": xxx, "message": "...", "data": null, "timestamp": xxx}
 *
 * @author Education Platform
 */
@Slf4j
@Order(-1)  // 优先级高于 DefaultErrorWebExceptionHandler（@Order(-1)）
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        int code;
        String message;

        if (ex instanceof TokenException tokenEx) {
            // JWT 相关异常（401）
            code = tokenEx.getCode();
            message = tokenEx.getMessage();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            log.warn("Token 认证失败: path={}, message={}", exchange.getRequest().getURI().getPath(), message);

        } else if (ex instanceof ResponseStatusException rse) {
            // Spring Cloud Gateway 路由未找到等（如404）
            code = rse.getStatusCode().value();
            message = rse.getReason() != null ? rse.getReason() : "请求处理失败";
            response.setStatusCode(rse.getStatusCode());
            log.warn("ResponseStatusException: path={}, status={}, reason={}",
                    exchange.getRequest().getURI().getPath(), code, message);

        } else {
            // 其他未知异常（500）
            code = 500;
            message = "服务器内部错误，请稍后重试";
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            log.error("网关未知异常: path={}", exchange.getRequest().getURI().getPath(), ex);
        }

        // 构造与 Result<T> 结构一致的响应 JSON
        Map<String, Object> errorBody = new HashMap<>(4);
        errorBody.put("code", code);
        errorBody.put("message", message);
        errorBody.put("data", null);
        errorBody.put("timestamp", System.currentTimeMillis());

        byte[] bytes = JSON.toJSONString(errorBody).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}

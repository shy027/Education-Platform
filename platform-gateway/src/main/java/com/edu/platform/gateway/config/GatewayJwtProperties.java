package com.edu.platform.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关自定义配置属性
 * 对应 application.yml 中的 gateway.* 配置项
 * <p>
 * 注意：不能命名为 GatewayProperties，与 Spring Cloud Gateway 框架内部 Bean 名称冲突
 *
 * @author Education Platform
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayJwtProperties {

    /**
     * JWT 白名单路径列表（无需 Token 即可访问的路径，支持 Ant 通配符）
     */
    private List<String> whiteList = new ArrayList<>();
}

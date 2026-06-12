package com.edu.platform.report.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 本地文件存储配置属性
 *
 * @author Education Platform
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "local.storage")
public class LocalStorageProperties {

    /** 本地文件存储根路径，例如 /home/uadmin/deploy/uploads */
    private String path;

    /** 对外访问的基础URL，例如 http://10.54.0.36 */
    private String baseUrl;

}

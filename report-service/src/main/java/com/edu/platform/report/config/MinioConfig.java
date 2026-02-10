package com.edu.platform.report.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置
 * 注: 实际项目使用阿里云OSS,此配置暂时保留但不启用
 *
 * @author Education Platform
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {
    
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    
    // TODO: Day 6 使用阿里云OSS替代MinIO
    // @Bean
    // public MinioClient minioClient() {
    //     return MinioClient.builder()
    //             .endpoint(endpoint)
    //             .credentials(accessKey, secretKey)
    //             .build();
    // }
    
}

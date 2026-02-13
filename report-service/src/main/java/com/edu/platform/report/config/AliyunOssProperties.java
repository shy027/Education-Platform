package com.edu.platform.report.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS配置属性
 *
 * @author Education Platform
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOssProperties {
    
    /**
     * OSS endpoint
     */
    private String endpoint;
    
    /**
     * Access Key ID
     */
    private String accessKeyId;
    
    /**
     * Access Key Secret
     */
    private String accessKeySecret;
    
    /**
     * Bucket名称
     */
    private String bucketName;
    
    /**
     * 文件夹路径
     */
    private String folder;
}

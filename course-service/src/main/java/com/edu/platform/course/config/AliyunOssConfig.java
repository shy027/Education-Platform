package com.edu.platform.course.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置
 *
 * @author Education Platform
 */
@Configuration
@RequiredArgsConstructor
public class AliyunOssConfig {
    
    private final AliyunOssProperties ossProperties;
    
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(
            ossProperties.getEndpoint(),
            ossProperties.getAccessKeyId(),
            ossProperties.getAccessKeySecret()
        );
    }
}

package com.edu.platform.community.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 环信IM配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "easemob")
public class EasemobConfig {
    
    /**
     * AppKey (格式: orgName#appName)
     */
    private String appKey;
    
    /**
     * ClientId
     */
    private String clientId;
    
    /**
     * ClientSecret
     */
    private String clientSecret;
    
    /**
     * 组织名称
     */
    private String orgName;
    
    /**
     * 应用名称
     */
    private String appName;
    
    /**
     * REST API配置
     */
    private RestApi restApi = new RestApi();
    
    /**
     * 群组配置
     */
    private Group group = new Group();
    
    @Data
    public static class RestApi {
        private String baseUrl = "https://a1.easemob.com";
        private Integer timeout = 30000;
    }
    
    @Data
    public static class Group {
        private Integer maxMembers = 200;
        private Boolean allowInvite = true;
    }
}

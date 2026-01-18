package com.edu.platform.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 容联云配置属性
 *
 * @author Education Platform
 */
@Data
@Component
@ConfigurationProperties(prefix = "ronglian")
public class RongLianProperties {
    
    /**
     * 账户SID
     */
    private String accountSid;
    
    /**
     * 认证令牌
     */
    private String authToken;
    
    /**
     * 应用ID
     */
    private String appId;
    
    /**
     * 短信模板ID
     */
    private String templateId;
    
    /**
     * 开发模式(true:模拟发送 false:真实发送)
     */
    private Boolean devMode = true;
    
}

package com.edu.platform.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 环信登录凭证响应
 */
@Data
@Schema(description = "环信登录凭证响应")
public class EasemobCredentialsResponse {
    
    @Schema(description = "环信AppKey", example = "1187260208225926#demo")
    private String appKey;
    
    @Schema(description = "环信用户名(即userId)", example = "123")
    private String username;
    
    @Schema(description = "用户ID", example = "123")
    private Long userId;
    
    @Schema(description = "用户真实姓名", example = "张三")
    private String realName;
    
    @Schema(description = "REST API基础URL", example = "https://a1.easemob.com")
    private String restApiUrl;
}

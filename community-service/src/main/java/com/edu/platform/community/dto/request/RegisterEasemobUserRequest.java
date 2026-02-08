package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册环信用户请求
 */
@Data
@Schema(description = "注册环信用户请求")
public class RegisterEasemobUserRequest {
    
    @NotBlank(message = "密码不能为空")
    @Schema(description = "环信登录密码", example = "password123")
    private String password;
}

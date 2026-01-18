package com.edu.platform.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 手机号登录请求
 *
 * @author Education Platform
 */
@Data
public class PhoneLoginRequest {
    
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 登录类型 (1:密码登录 2:验证码登录)
     */
    @NotNull(message = "登录类型不能为空")
    private Integer loginType;
    
    /**
     * 密码(密码登录时必填)
     */
    private String password;
    
    /**
     * 验证码(验证码登录时必填)
     */
    private String verifyCode;
    
}

package com.edu.platform.user.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * 修改个人信息请求
 *
 * @author Education Platform
 */
@Data
public class UpdateProfileRequest {
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 性别 (0:未知 1:男 2:女)
     */
    private Integer gender;
    
}

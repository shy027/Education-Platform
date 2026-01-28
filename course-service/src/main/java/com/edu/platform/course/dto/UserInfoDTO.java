package com.edu.platform.course.dto;

import lombok.Data;

/**
 * 用户信息DTO
 * 用于Feign调用获取用户基本信息
 *
 * @author Education Platform
 */
@Data
public class UserInfoDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名（用作学号）
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
}

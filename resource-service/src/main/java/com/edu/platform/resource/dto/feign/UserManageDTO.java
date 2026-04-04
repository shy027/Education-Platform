package com.edu.platform.resource.dto.feign;

import lombok.Data;

/**
 * 远程调用用户服务返回的用户信息
 */
@Data
public class UserManageDTO {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 用户名
     */
    private String username;
}

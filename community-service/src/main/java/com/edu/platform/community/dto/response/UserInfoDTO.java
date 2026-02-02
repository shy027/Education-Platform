package com.edu.platform.community.dto.response;

import lombok.Data;

/**
 * 用户信息响应(简化版)
 *
 * @author Education Platform
 */
@Data
public class UserInfoDTO {
    
    private Long id;
    
    private String realName;
    
    private String avatarUrl;
    
    private Long roleId;
    
    private String roleName;
    
}

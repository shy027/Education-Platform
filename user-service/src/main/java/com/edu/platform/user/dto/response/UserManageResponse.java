package com.edu.platform.user.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理响应
 *
 * @author Education Platform
 */
@Data
public class UserManageResponse {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
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
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 性别 (0:未知 1:男 2:女)
     */
    private Integer gender;
    
    /**
     * 状态 (0:禁用 1:正常)
     */
    private Integer status;
    
    /**
     * 角色列表
     */
    private List<RoleInfo> roles;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 注册时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 角色信息
     */
    @Data
    public static class RoleInfo {
        private Long roleId;
        private String roleName;
        private String roleCode;
    }
    
}

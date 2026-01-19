package com.edu.platform.user.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色响应
 *
 * @author Education Platform
 */
@Data
public class RoleResponse {
    
    /**
     * 角色ID
     */
    private Long id;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色编码
     */
    private String roleCode;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 状态 (0:禁用 1:启用)
     */
    private Integer status;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
}

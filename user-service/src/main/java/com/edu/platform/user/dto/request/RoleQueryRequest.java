package com.edu.platform.user.dto.request;

import lombok.Data;

/**
 * 角色查询请求
 *
 * @author Education Platform
 */
@Data
public class RoleQueryRequest {
    
    /**
     * 角色名称(模糊查询)
     */
    private String roleName;
    
    /**
     * 角色编码
     */
    private String roleCode;
    
    /**
     * 状态 (0:禁用 1:启用)
     */
    private Integer status;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
}

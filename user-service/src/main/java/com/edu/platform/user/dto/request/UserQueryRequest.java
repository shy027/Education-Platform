package com.edu.platform.user.dto.request;

import lombok.Data;

/**
 * 用户查询请求
 *
 * @author Education Platform
 */
@Data
public class UserQueryRequest {
    
    /**
     * 用户名(模糊查询)
     */
    private String username;
    
    /**
     * 真实姓名(模糊查询)
     */
    private String realName;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 学校ID
     */
    private Long schoolId;
    
    /**
     * 状态 (0:禁用 1:正常)
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

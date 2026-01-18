package com.edu.platform.user.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 当前用户信息响应
 *
 * @author Education Platform
 */
@Data
public class CurrentUserResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
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
     * 头像
     */
    private String avatar;
    
    /**
     * 性别
     */
    private Integer gender;
    
    /**
     * 角色列表
     */
    private List<String> roles;
    
    /**
     * 学校列表
     */
    private List<SchoolInfo> schools;
    
    @Data
    public static class SchoolInfo {
        /**
         * 学校ID
         */
        private Long schoolId;
        
        /**
         * 学校名称
         */
        private String schoolName;
        
        /**
         * 成员类型
         */
        private Integer memberType;
    }
    
}

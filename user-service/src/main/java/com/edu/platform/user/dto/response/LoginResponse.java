package com.edu.platform.user.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 登录响应
 *
 * @author Education Platform
 */
@Data
public class LoginResponse {
    
    /**
     * Token
     */
    private String token;
    
    /**
     * 用户信息
     */
    private UserInfo userInfo;
    
    @Data
    public static class UserInfo {
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
         * 头像
         */
        private String avatar;
        
        /**
         * 角色列表
         */
        private List<String> roles;
    }
    
}

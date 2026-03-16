package com.edu.platform.community.dto.response;

import lombok.Data;
import java.util.List;

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
    
    // 兼容字段
    private Long roleId;
    private String roleName;
    
    /**
     * 角色列表 (对应 user-service 的 UserManageResponse)
     */
    private List<RoleInfo> roles;
    
    @Data
    public static class RoleInfo {
        private Long roleId;
        private String roleName;
        private String roleCode;
    }
}

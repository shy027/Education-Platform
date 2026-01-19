package com.edu.platform.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户状态请求
 *
 * @author Education Platform
 */
@Data
public class UserStatusRequest {
    
    /**
     * 状态 (0:禁用 1:启用)
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
    
}

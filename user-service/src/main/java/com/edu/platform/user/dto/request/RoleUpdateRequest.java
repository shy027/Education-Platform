package com.edu.platform.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新角色请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "更新角色请求")
public class RoleUpdateRequest {
    
    @Schema(description = "角色名称", example = "高级教师")
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    
    @Schema(description = "角色描述", example = "拥有更多教学权限")
    private String description;
    
    @Schema(description = "排序", example = "3")
    private Integer sortOrder;
    
    @Schema(description = "状态 (0:禁用 1:正常)", example = "1")
    private Integer status;
    
}

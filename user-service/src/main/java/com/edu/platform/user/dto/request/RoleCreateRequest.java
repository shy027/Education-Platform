package com.edu.platform.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 创建角色请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "创建角色请求")
public class RoleCreateRequest {
    
    @Schema(description = "角色编码", example = "TEACHER_ASSISTANT")
    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[A-Z_]+$", message = "角色编码只能包含大写字母和下划线")
    private String roleCode;
    
    @Schema(description = "角色名称", example = "助教")
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    
    @Schema(description = "角色描述", example = "协助教师管理课程")
    private String description;
    
    @Schema(description = "排序", example = "5")
    private Integer sortOrder;
    
}

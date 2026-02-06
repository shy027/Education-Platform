package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建小组请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "创建小组请求")
public class CreateGroupRequest {
    
    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    @Schema(description = "小组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "小组名称不能为空")
    private String groupName;
    
    @Schema(description = "小组简介")
    private String groupIntro;
    
    @Schema(description = "最大成员数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "最大成员数不能为空")
    @Min(value = 1, message = "最大成员数至少为1")
    private Integer maxMembers;
}

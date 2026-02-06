package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新小组请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "更新小组请求")
public class UpdateGroupRequest {
    
    @Schema(description = "小组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "小组名称不能为空")
    private String groupName;
    
    @Schema(description = "小组简介")
    private String groupIntro;
    
    @Schema(description = "最大成员数")
    @Min(value = 1, message = "最大成员数至少为1")
    private Integer maxMembers;
}

package com.edu.platform.resource.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新标签请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "更新标签请求")
public class TagUpdateRequest {
    
    @Schema(description = "标签名称", example = "爱国主义")
    @NotBlank(message = "标签名称不能为空")
    private String tagName;
    
    @Schema(description = "标签颜色", example = "#F56C6C")
    private String tagColor;
    
    @Schema(description = "所属分类ID", example = "1")
    private Long categoryId;
    
    @Schema(description = "标签描述", example = "培养爱国主义精神和民族自豪感")
    private String description;
    
    @Schema(description = "排序", example = "1")
    private Integer sortOrder;
    
    @Schema(description = "状态 (0:禁用 1:启用)", example = "1")
    private Integer status;
    
}

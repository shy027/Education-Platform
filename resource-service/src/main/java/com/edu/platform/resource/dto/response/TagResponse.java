package com.edu.platform.resource.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签响应
 *
 * @author Education Platform
 */
@Data
@Schema(description = "标签响应")
public class TagResponse {
    
    @Schema(description = "标签ID")
    private Long id;
    
    @Schema(description = "标签名称")
    private String tagName;
    
    @Schema(description = "标签分类")
    private String tagCategory;
    
    @Schema(description = "标签描述")
    private String description;
    
    @Schema(description = "使用次数")
    private Integer useCount;
    
    @Schema(description = "排序")
    private Integer sortOrder;
    
    @Schema(description = "状态 (0:禁用 1:启用)")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
    
}

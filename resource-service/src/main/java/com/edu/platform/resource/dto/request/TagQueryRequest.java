package com.edu.platform.resource.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标签查询请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "标签查询请求")
public class TagQueryRequest {
    
    @Schema(description = "标签名称(模糊查询)", example = "爱国")
    private String tagName;
    
    @Schema(description = "标签分类", example = "核心价值观")
    private String tagCategory;
    
    @Schema(description = "状态 (0:禁用 1:启用)", example = "1")
    private Integer status;
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum;
    
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize;
    
}

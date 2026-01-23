package com.edu.platform.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建分类请求DTO
 *
 * @author Education Platform
 */
@Data
public class CategoryCreateRequest {
    
    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;
    
    /**
     * 父分类ID, 0表示顶级
     */
    @NotNull(message = "父分类ID不能为空")
    private Long parentId;
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
    
    /**
     * 分类图标
     */
    private String icon;
    
    /**
     * 分类描述
     */
    private String description;
    
}

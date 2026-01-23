package com.edu.platform.resource.dto.request;

import lombok.Data;

/**
 * 更新分类请求DTO
 *
 * @author Education Platform
 */
@Data
public class CategoryUpdateRequest {
    
    /**
     * 分类名称
     */
    private String categoryName;
    
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
    
    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
    
}

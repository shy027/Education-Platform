package com.edu.platform.resource.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 分类响应DTO
 *
 * @author Education Platform
 */
@Data
public class CategoryResponse {
    
    /**
     * 分类ID
     */
    private Long id;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 父分类ID
     */
    private Long parentId;
    
    /**
     * 层级
     */
    private Integer level;
    
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
     * 状态
     */
    private Integer status;
    
    /**
     * 子分类列表
     */
    private List<CategoryResponse> children;
    
}

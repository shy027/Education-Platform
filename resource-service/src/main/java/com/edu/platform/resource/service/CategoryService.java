package com.edu.platform.resource.service;

import com.edu.platform.resource.dto.request.CategoryCreateRequest;
import com.edu.platform.resource.dto.request.CategoryUpdateRequest;
import com.edu.platform.resource.dto.response.CategoryResponse;

import java.util.List;

/**
 * 分类服务接口
 *
 * @author Education Platform
 */
public interface CategoryService {
    
    /**
     * 获取分类树
     *
     * @return 分类树列表
     */
    List<CategoryResponse> getCategoryTree();
    
    /**
     * 获取子分类列表(一级)
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<CategoryResponse> getChildren(Long parentId);
    
    /**
     * 创建分类
     *
     * @param request 创建请求
     * @return 分类ID
     */
    Long createCategory(CategoryCreateRequest request);
    
    /**
     * 更新分类
     *
     * @param id 分类ID
     * @param request 更新请求
     */
    void updateCategory(Long id, CategoryUpdateRequest request);
    
    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);
    
}

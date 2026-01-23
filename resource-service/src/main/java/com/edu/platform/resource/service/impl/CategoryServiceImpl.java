package com.edu.platform.resource.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.resource.dto.request.CategoryCreateRequest;
import com.edu.platform.resource.dto.request.CategoryUpdateRequest;
import com.edu.platform.resource.dto.response.CategoryResponse;
import com.edu.platform.resource.entity.ResourceCategory;
import com.edu.platform.resource.mapper.ResourceCategoryMapper;
import com.edu.platform.resource.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final ResourceCategoryMapper categoryMapper;
    
    @Override
    public List<CategoryResponse> getCategoryTree() {
        // 获取所有分类
        LambdaQueryWrapper<ResourceCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ResourceCategory::getSortOrder);
        List<ResourceCategory> allCategories = categoryMapper.selectList(wrapper);
        
        // 转换为DTO
        List<CategoryResponse> allResponses = allCategories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        // 构建树形结构
        return buildTree(allResponses, 0L);
    }
    
    @Override
    public List<CategoryResponse> getChildren(Long parentId) {
        LambdaQueryWrapper<ResourceCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceCategory::getParentId, parentId);
        wrapper.orderByAsc(ResourceCategory::getSortOrder);
        List<ResourceCategory> categories = categoryMapper.selectList(wrapper);
        
        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(CategoryCreateRequest request) {
        // 检查名称重复
        checkNameExists(request.getCategoryName(), null);
        
        ResourceCategory category = new ResourceCategory();
        BeanUtil.copyProperties(request, category);
        
        // 设置层级
        if (request.getParentId() == 0) {
            category.setLevel(1);
        } else {
            ResourceCategory parent = categoryMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
        }
        
        category.setStatus(1); // 默认启用
        categoryMapper.insert(category);
        
        return category.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, CategoryUpdateRequest request) {
        ResourceCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        if (StrUtil.isNotBlank(request.getCategoryName())) {
            checkNameExists(request.getCategoryName(), id);
            category.setCategoryName(request.getCategoryName());
        }
        
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }
        
        categoryMapper.updateById(category);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        ResourceCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查是否有子分类
        LambdaQueryWrapper<ResourceCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceCategory::getParentId, id);
        if (categoryMapper.exists(wrapper)) {
            throw new BusinessException("存在子分类,无法删除");
        }
        
        // TODO: 检查是否被资源引用
        
        categoryMapper.deleteById(id);
    }
    
    /**
     * 构建树形结构
     */
    private List<CategoryResponse> buildTree(List<CategoryResponse> all, Long parentId) {
        return all.stream()
                .filter(node -> node.getParentId().equals(parentId))
                .peek(node -> node.setChildren(buildTree(all, node.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * DTO转换
     */
    private CategoryResponse convertToResponse(ResourceCategory entity) {
        return BeanUtil.copyProperties(entity, CategoryResponse.class);
    }
    
    /**
     * 检查名称是否重复
     */
    private void checkNameExists(String name, Long excludeId) {
        LambdaQueryWrapper<ResourceCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceCategory::getCategoryName, name);
        if (excludeId != null) {
            wrapper.ne(ResourceCategory::getId, excludeId);
        }
        if (categoryMapper.exists(wrapper)) {
            throw new BusinessException("分类名称已存在");
        }
    }
    
}

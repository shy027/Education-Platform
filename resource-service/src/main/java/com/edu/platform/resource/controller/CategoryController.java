package com.edu.platform.resource.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.resource.dto.request.CategoryCreateRequest;
import com.edu.platform.resource.dto.request.CategoryUpdateRequest;
import com.edu.platform.resource.dto.response.CategoryResponse;
import com.edu.platform.resource.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器
 *
 * @author Education Platform
 */
@Tag(name = "分类管理", description = "资源分类管理相关接口")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<CategoryResponse>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }
    
    @Operation(summary = "获取子分类列表")
    @GetMapping("/{parentId}/children")
    public Result<List<CategoryResponse>> getChildren(@PathVariable Long parentId) {
        return Result.success(categoryService.getChildren(parentId));
    }
    
    @Operation(summary = "创建分类")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Long> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        return Result.success(categoryService.createCategory(request));
    }
    
    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody CategoryUpdateRequest request) {
        categoryService.updateCategory(id, request);
        return Result.success();
    }
    
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
    
}

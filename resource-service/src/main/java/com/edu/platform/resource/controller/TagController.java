package com.edu.platform.resource.controller;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.resource.dto.request.TagCreateRequest;
import com.edu.platform.resource.dto.request.TagQueryRequest;
import com.edu.platform.resource.dto.request.TagUpdateRequest;
import com.edu.platform.resource.dto.response.TagResponse;
import com.edu.platform.resource.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理控制器
 *
 * @author Education Platform
 */
@Tag(name = "标签管理", description = "思政元素标签管理相关接口")
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    
    @Operation(summary = "标签列表查询(分页)")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<PageResult<TagResponse>> getTagList(
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) String tagCategory,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        TagQueryRequest request = new TagQueryRequest();
        request.setTagName(tagName);
        request.setTagCategory(tagCategory);
        request.setStatus(status);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        PageResult<TagResponse> result = tagService.getTagList(request);
        return Result.success(result);
    }
    
    @Operation(summary = "创建标签")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Long> createTag(@Valid @RequestBody TagCreateRequest request) {
        Long tagId = tagService.createTag(request);
        return Result.success(tagId);
    }
    
    @Operation(summary = "更新标签")
    @PutMapping("/{tagId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> updateTag(
            @PathVariable Long tagId,
            @Valid @RequestBody TagUpdateRequest request) {
        tagService.updateTag(tagId, request);
        return Result.success();
    }
    
    @Operation(summary = "删除标签")
    @DeleteMapping("/{tagId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> deleteTag(@PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return Result.success();
    }
    
    @Operation(summary = "获取所有启用的标签(不分页)")
    @GetMapping("/enabled")
    public Result<List<TagResponse>> getAllEnabledTags() {
        List<TagResponse> list = tagService.getAllEnabledTags();
        return Result.success(list);
    }
    
}

package com.edu.platform.resource.controller;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.common.utils.JwtUtil;
import com.edu.platform.resource.dto.request.ResourceAuditRequest;
import com.edu.platform.resource.dto.request.ResourceCreateRequest;
import com.edu.platform.resource.dto.request.ResourceQueryRequest;
import com.edu.platform.resource.dto.request.ResourceUpdateRequest;
import com.edu.platform.resource.dto.response.AuditLogResponse;
import com.edu.platform.resource.dto.response.ResourceDetailResponse;
import com.edu.platform.resource.dto.response.ResourceResponse;
import com.edu.platform.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.edu.platform.common.annotation.RequireAdminOrLeader;
import com.edu.platform.common.annotation.RequireTeacherOrAbove;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源管理控制器
 *
 * @author Education Platform
 */
@Tag(name = "资源管理", description = "资源CRUD、审核流程相关接口")
@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {
    
    private final ResourceService resourceService;
    
    @Operation(summary = "创建资源")
    @PostMapping
    @RequireTeacherOrAbove
    public Result<Long> createResource(
            @Valid @RequestBody ResourceCreateRequest request,
            HttpServletRequest httpRequest) {
        
        String token = getTokenFromRequest(httpRequest);
        Long userId = JwtUtil.getUserId(token);
        String userRole = JwtUtil.getRoles(token).get(0); // 获取第一个角色
        
        Long resourceId = resourceService.createResource(request, userId, userRole);
        return Result.success(resourceId);
    }
    
    @Operation(summary = "更新资源")
    @PutMapping("/{id}")
    @RequireTeacherOrAbove
    public Result<Void> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceUpdateRequest request,
            HttpServletRequest httpRequest) {
        
        String token = getTokenFromRequest(httpRequest);
        Long userId = JwtUtil.getUserId(token);
        
        resourceService.updateResource(id, request, userId);
        return Result.success();
    }
    
    @Operation(summary = "删除资源")
    @DeleteMapping("/{id}")
    @RequireTeacherOrAbove
    public Result<Void> deleteResource(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        String token = getTokenFromRequest(httpRequest);
        Long userId = JwtUtil.getUserId(token);
        
        resourceService.deleteResource(id, userId);
        return Result.success();
    }
    
    @Operation(summary = "获取资源详情")
    @GetMapping("/{id}")
    public Result<ResourceDetailResponse> getResourceDetail(@PathVariable Long id) {
        // 增加浏览次数
        resourceService.incrementViewCount(id);
        
        ResourceDetailResponse response = resourceService.getResourceDetail(id);
        return Result.success(response);
    }
    
    @Operation(summary = "分页查询资源列表")
    @GetMapping
    public Result<PageResult<ResourceResponse>> getResourceList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        ResourceQueryRequest request = new ResourceQueryRequest();
        request.setKeyword(keyword);
        request.setCategoryId(categoryId);
        request.setStatus(status);
        request.setCreatorId(creatorId);
        request.setTagId(tagId);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        PageResult<ResourceResponse> result = resourceService.getResourceList(request);
        return Result.success(result);
    }
    
    @Operation(summary = "提交审核")
    @PostMapping("/{id}/submit")
    @RequireTeacherOrAbove
    public Result<Void> submitForAudit(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        String token = getTokenFromRequest(httpRequest);
        Long userId = JwtUtil.getUserId(token);
        
        resourceService.submitForAudit(id, userId);
        return Result.success();
    }
    
    @Operation(summary = "审核资源")
    @PostMapping("/{id}/audit")
    @RequireAdminOrLeader
    public Result<Void> auditResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceAuditRequest request,
            HttpServletRequest httpRequest) {
        
        String token = getTokenFromRequest(httpRequest);
        Long auditorId = JwtUtil.getUserId(token);
        
        resourceService.auditResource(id, request, auditorId);
        return Result.success();
    }
    
    @Operation(summary = "获取待审核列表")
    @GetMapping("/pending")
    @RequireAdminOrLeader
    public Result<PageResult<ResourceResponse>> getPendingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageResult<ResourceResponse> result = resourceService.getPendingList(pageNum, pageSize);
        return Result.success(result);
    }
    
    @Operation(summary = "获取审核历史")
    @GetMapping("/{id}/audit-logs")
    public Result<List<AuditLogResponse>> getAuditLogs(@PathVariable Long id) {
        return Result.success(resourceService.getAuditLogs(id));
    }
    
    @Operation(summary = "下架资源")
    @PostMapping("/{id}/offline")
    @RequireAdminOrLeader
    public Result<Void> offlineResource(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        String token = getTokenFromRequest(httpRequest);
        Long userId = JwtUtil.getUserId(token);
        
        resourceService.offlineResource(id, userId);
        return Result.success();
    }
    
    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
}

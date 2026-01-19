package com.edu.platform.user.controller;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.user.dto.request.RoleQueryRequest;
import com.edu.platform.user.dto.response.RoleResponse;
import com.edu.platform.user.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 *
 * @author Education Platform
 */
@Tag(name = "角色管理", description = "角色管理相关接口")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;
    
    @Operation(summary = "角色列表查询(分页)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<RoleResponse>> getRoleList(
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        RoleQueryRequest request = new RoleQueryRequest();
        request.setRoleName(roleName);
        request.setRoleCode(roleCode);
        request.setStatus(status);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        PageResult<RoleResponse> result = roleService.getRoleList(request);
        return Result.success(result);
    }
    
    @Operation(summary = "角色详情查询")
    @GetMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<RoleResponse> getRoleDetail(@PathVariable Long roleId) {
        RoleResponse response = roleService.getRoleDetail(roleId);
        return Result.success(response);
    }
    
    @Operation(summary = "获取所有角色(不分页)")
    @GetMapping("/all")
    public Result<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> list = roleService.getAllRoles();
        return Result.success(list);
    }
    
}

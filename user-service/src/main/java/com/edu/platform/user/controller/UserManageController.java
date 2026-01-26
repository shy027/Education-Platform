package com.edu.platform.user.controller;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.user.dto.request.UserQueryRequest;
import com.edu.platform.user.dto.request.UserStatusRequest;
import com.edu.platform.user.dto.response.UserManageResponse;
import com.edu.platform.user.service.ExcelService;
import com.edu.platform.user.service.UserManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.edu.platform.common.annotation.RequireAdminOrLeader;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器
 *
 * @author Education Platform
 */
@Tag(name = "用户管理", description = "用户管理相关接口(管理员)")
@RestController
@RequestMapping("/api/v1/users/manage")
@RequiredArgsConstructor
public class UserManageController {
    
    private final UserManageService userManageService;
    private final ExcelService excelService;
    
    @Operation(summary = "用户列表查询(分页)")
    @GetMapping
    @RequireAdminOrLeader
    public Result<PageResult<UserManageResponse>> getUserList(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        UserQueryRequest request = new UserQueryRequest();
        request.setUsername(username);
        request.setRealName(realName);
        request.setPhone(phone);
        request.setEmail(email);
        request.setRoleId(roleId);
        request.setStatus(status);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        PageResult<UserManageResponse> result = userManageService.getUserList(request);
        return Result.success(result);
    }
    
    @Operation(summary = "用户详情查询")
    @GetMapping("/{userId}")
    @RequireAdminOrLeader
    public Result<UserManageResponse> getUserDetail(@PathVariable Long userId) {
        UserManageResponse response = userManageService.getUserDetail(userId);
        return Result.success(response);
    }
    
    @Operation(summary = "更新用户状态")
    @PutMapping("/{userId}/status")
    @RequireAdminOrLeader
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusRequest request) {
        userManageService.updateUserStatus(userId, request.getStatus());
        return Result.success("状态更新成功", null);
    }
    
    @Operation(summary = "重置用户密码")
    @PostMapping("/{userId}/reset-password")
    @RequireAdminOrLeader
    public Result<Map<String, Object>> resetPassword(@PathVariable Long userId) {
        String newPassword = userManageService.resetPassword(userId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("newPassword", newPassword);
        data.put("message", "密码已重置,请妥善保管");
        
        return Result.success("密码重置成功", data);
    }
    
    @Operation(summary = "下载用户导入模板")
    @GetMapping("/template")
    @RequireAdminOrLeader
    public void downloadTemplate(HttpServletResponse response) {
        excelService.downloadUserTemplate(response);
    }
    
    @Operation(summary = "批量导入用户")
    @PostMapping("/import")
    @RequireAdminOrLeader
    public Result<Map<String, Object>> importUsers(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = excelService.importUsers(file);
        return Result.success("导入完成", result);
    }
    
    @Operation(summary = "导出用户列表")
    @GetMapping("/export")
    @RequireAdminOrLeader
    public void exportUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) {
        
        UserQueryRequest request = new UserQueryRequest();
        request.setUsername(username);
        request.setRealName(realName);
        request.setPhone(phone);
        request.setSchoolId(schoolId);
        request.setStatus(status);
        
        excelService.exportUsers(request, response);
    }
    
}

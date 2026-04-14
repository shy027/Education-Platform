package com.edu.platform.user.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.user.service.SchoolService;
import com.edu.platform.user.service.UserManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 内部调用统计接口
 */
@Tag(name = "内部接口-用户统计")
@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserManageService userManageService;
    private final SchoolService schoolService;

    @Operation(summary = "获取用户看板统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = userManageService.getUserStats();
        stats.put("totalSchools", schoolService.getSchoolCount());
        return Result.success(stats);
    }

    @Operation(summary = "批量获取用户信息")
    @PostMapping("/batch")
    public Result<Map<Long, com.edu.platform.user.dto.response.UserManageResponse>> batchGetUserInfo(
            @RequestBody java.util.List<Long> userIds) {
        return Result.success(userManageService.batchGetUserInfo(userIds));
    }

    @Operation(summary = "获取单个用户信息")
    @GetMapping("/{userId}")
    public Result<com.edu.platform.user.dto.response.UserManageResponse> getUserById(@PathVariable Long userId) {
        return Result.success(userManageService.getUserDetail(userId));
    }

    @Operation(summary = "根据条件查询用户ID列表")
    @PostMapping("/search-ids")
    public Result<java.util.List<Long>> queryUserIds(@RequestBody Map<String, String> params) {
        String department = params.get("department");
        String className = params.get("className");
        return Result.success(userManageService.queryUserIds(department, className));
    }

    @Operation(summary = "根据用户ID列表获取去重后的学院和班级选项")
    @PostMapping("/filter-options")
    public Result<java.util.Map<String, java.util.List<String>>> getMemberFilterOptions(@RequestBody java.util.List<Long> userIds) {
        return Result.success(userManageService.getMemberFilterOptions(userIds));
    }
}

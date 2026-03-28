package com.edu.platform.user.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.user.service.SchoolService;
import com.edu.platform.user.service.UserManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}

package com.edu.platform.report.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.report.client.AuditClient;
import com.edu.platform.report.client.CourseClient;
import com.edu.platform.report.client.ResourceClient;
import com.edu.platform.report.client.UserClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员看板聚合接口
 */
@Tag(name = "管理员-数据看板")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserClient userClient;
    private final CourseClient courseClient;
    private final ResourceClient resourceClient;
    private final AuditClient auditClient;

    @Operation(summary = "获取全平台统计汇总数据")
    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_LEADER')")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> aggregateStats = new HashMap<>();
        
        // 1. 获取用户与学校统计
        try {
            Result<Map<String, Object>> userResult = userClient.getStats();
            if (userResult.isSuccess()) {
                aggregateStats.putAll(userResult.getData());
            }
        } catch (Exception e) {
            log.error("获取用户统计失败: {}", e.getMessage());
        }

        // 2. 获取课程统计
        try {
            Result<Map<String, Object>> courseResult = courseClient.getStats();
            if (courseResult.isSuccess()) {
                aggregateStats.putAll(courseResult.getData());
            }
        } catch (Exception e) {
            log.error("获取课程统计失败: {}", e.getMessage());
        }

        // 3. 获取资源统计
        try {
            Result<Map<String, Object>> resourceResult = resourceClient.getStats();
            if (resourceResult.isSuccess()) {
                aggregateStats.putAll(resourceResult.getData());
            }
        } catch (Exception e) {
            log.error("获取资源统计失败: {}", e.getMessage());
        }

        // 4. 获取审核统计
        try {
            Result<Map<String, Object>> auditResult = auditClient.getStats();
            if (auditResult.isSuccess()) {
                aggregateStats.putAll(auditResult.getData());
            }
        } catch (Exception e) {
            log.error("获取审核统计失败: {}", e.getMessage());
        }

        return Result.success(aggregateStats);
    }
}

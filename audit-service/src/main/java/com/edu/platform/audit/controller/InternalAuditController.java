package com.edu.platform.audit.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.audit.service.AuditService;
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
@Tag(name = "内部接口-审核统计")
@RestController
@RequestMapping("/internal/audit")
@RequiredArgsConstructor
public class InternalAuditController {

    private final AuditService auditService;

    @Operation(summary = "获取审核看板统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getAuditStats() {
        return Result.success(auditService.getAuditStats());
    }
}

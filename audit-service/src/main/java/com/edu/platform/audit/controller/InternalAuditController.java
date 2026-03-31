package com.edu.platform.audit.controller;

import com.edu.platform.audit.dto.request.AuditQueryRequest;
import com.edu.platform.audit.dto.response.AuditRecordVO;
import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 内部调用统计与查询接口
 */
@Tag(name = "内部接口-审核中转")
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

    @Operation(summary = "查询审核记录 (内部调用)")
    @GetMapping("/records")
    public Result<PageResult<AuditRecordVO>> getAuditRecords(
            @Parameter(description = "内容类型") @RequestParam(required = false) String contentType,
            @Parameter(description = "内容ID") @RequestParam(required = false) Long contentId) {
        
        AuditQueryRequest request = new AuditQueryRequest();
        request.setContentType(contentType);
        request.setContentId(contentId);
        request.setPageNum(1);
        request.setPageSize(100); // 内部查询通常获取全部相关记录
        
        PageResult<AuditRecordVO> result = auditService.getAuditRecords(request);
        return Result.success(result);
    }
}

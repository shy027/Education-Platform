package com.edu.platform.audit.controller;

import com.edu.platform.audit.dto.request.AuditQueryRequest;
import com.edu.platform.audit.dto.request.AuditRequest;
import com.edu.platform.audit.dto.request.BatchAuditRequest;
import com.edu.platform.audit.dto.request.ManualAuditRecordRequest;
import com.edu.platform.audit.dto.response.AuditRecordVO;
import com.edu.platform.audit.dto.response.BatchAuditResult;
import com.edu.platform.audit.service.AuditService;
import com.edu.platform.common.annotation.RequireAdmin;
import com.edu.platform.common.annotation.RequireAdminOrLeader;
import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.common.utils.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 审核管理控制器
 *
 * @author Education Platform
 */
@Tag(name = "审核管理", description = "内容审核相关接口")
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {
    
    private final AuditService auditService;
    
    @Operation(summary = "查询待审核列表")
    @GetMapping("/pending")
    @RequireAdmin
    public Result<PageResult<AuditRecordVO>> getPendingList(
            @Parameter(description = "内容类型: COURSEWARE/POST/COMMENT") @RequestParam(required = false) String contentType,
            @Parameter(description = "风险等级: 1-低, 2-中, 3-高") @RequestParam(required = false) Integer riskLevel,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        AuditQueryRequest request = new AuditQueryRequest();
        request.setContentType(contentType);
        request.setRiskLevel(riskLevel);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        PageResult<AuditRecordVO> result = auditService.getPendingList(request);
        return Result.success(result);
    }
    
    @Operation(summary = "人工审核")
    @PutMapping("/{recordId}")
    @RequireAdminOrLeader
    public Result<Void> manualAudit(
            @Parameter(description = "审核记录ID") @PathVariable Long recordId,
            @Valid @RequestBody AuditRequest request) {
        
        Long auditorId = UserContext.getUserId();
        auditService.manualAudit(recordId, request, auditorId);
        return Result.success();
    }
    
    @Operation(summary = "批量审核")
    @PutMapping("/batch")
    @RequireAdminOrLeader
    public Result<BatchAuditResult> batchAudit(@Valid @RequestBody BatchAuditRequest request) {
        Long auditorId = UserContext.getUserId();
        BatchAuditResult result = auditService.batchAudit(request, auditorId);
        return Result.success(result);
    }
    
    @Operation(summary = "查询审核记录")
    @GetMapping("/records")
    @RequireAdmin
    public Result<PageResult<AuditRecordVO>> getAuditRecords(
            @Parameter(description = "内容类型") @RequestParam(required = false) String contentType,
            @Parameter(description = "审核结果") @RequestParam(required = false) Integer auditResult,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        AuditQueryRequest request = new AuditQueryRequest();
        request.setContentType(contentType);
        request.setAuditResult(auditResult);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        PageResult<AuditRecordVO> result = auditService.getAuditRecords(request);
        return Result.success(result);
    }

    @Operation(summary = "手动审核结果上报 (内部调用)")
    @PostMapping("/manual/record")
    public Result<Void> recordManualAudit(@Valid @RequestBody ManualAuditRecordRequest request) {
        auditService.recordManualAudit(
                request.getContentType(),
                request.getContentId(),
                request.getAuditResult(),
                request.getAuditReason(),
                request.getAuditorId()
        );
        return Result.success();
    }

    @Operation(summary = "提交审核申请 (内部调用)")
    @PostMapping("/submit")
    public Result<Void> submitAuditRequest(
            @RequestParam String contentType,
            @RequestParam Long contentId) {
        auditService.submitAuditRequest(contentType, contentId);
        return Result.success();
    }
}

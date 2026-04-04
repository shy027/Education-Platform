package com.edu.platform.resource.client;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.resource.dto.client.AuditRecordVO;
import com.edu.platform.resource.dto.client.ManualAuditRecordRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 审核服务 Feign 客户端
 *
 * @author Education Platform
 */
@FeignClient(
    name = "audit-service", 
    contextId = "resourceAuditClient", 
    url = "${app.feign.services.audit-service.url:http://localhost:8086}", 
    path = "/api/v1/audit"
)
public interface AuditClient {

    /**
     * 手动审核结果上报
     *
     * @param request 记录请求
     * @return 结果
     */
    @PostMapping("/manual/record")
    Result<Void> recordManualAudit(@RequestBody ManualAuditRecordRequest request);

    /**
     * 提交审核申请
     */
    @PostMapping("/submit")
    Result<Void> submitAuditRequest(
            @RequestParam("contentType") String contentType,
            @RequestParam("contentId") Long contentId);

    /**
     * 查询审核记录
     */
    @GetMapping("/records")
    Result<PageResult<AuditRecordVO>> getAuditRecords(
            @RequestParam(value = "contentType", required = false) String contentType,
            @RequestParam(value = "contentId", required = false) Long contentId
    );
}

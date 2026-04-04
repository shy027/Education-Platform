package com.edu.platform.course.client;

import com.edu.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 审核服务Feign客户端
 */
@FeignClient(
    name = "audit-service", 
    url = "${app.feign.services.audit-service.url:http://localhost:8086}", 
    path = "/api/v1/audit"
)
public interface AuditClient {

    /**
     * 提交审核申请
     */
    @PostMapping("/submit")
    Result<Void> submitAuditRequest(
            @RequestParam("contentType") String contentType,
            @RequestParam("contentId") Long contentId);

    /**
     * 上报人工审核结果
     */
    @PostMapping("/manual/record")
    Result<Void> recordManualAudit(@RequestBody Map<String, Object> request);
}

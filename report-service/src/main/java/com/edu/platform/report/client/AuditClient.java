package com.edu.platform.report.client;

import com.edu.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * 审核服务Feign客户端
 */
@FeignClient(
    name = "audit-service", 
    url = "${app.feign.services.audit-service.url:http://localhost:8086}", 
    path = "/internal/audit"
)
public interface AuditClient {

    /**
     * 获取审核看板统计数据
     */
    @GetMapping("/stats")
    Result<Map<String, Object>> getStats();
}

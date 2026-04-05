package com.edu.platform.audit.client;

import com.edu.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 资源服务Feign客户端
 *
 * @author Education Platform
 */
@FeignClient(name = "resource-service", url = "${app.feign.services.resource-service.url:http://localhost:8082}", path = "/internal/resource")
public interface ResourceClient {

    /**
     * 更新资源审核状态
     *
     * @param resourceId 资源ID
     * @param request    请求体 (auditStatus, auditorId, auditRemark)
     */
    @PutMapping("/{resourceId}/audit-status")
    Result<Void> updateAuditStatus(@PathVariable("resourceId") Long resourceId,
                                   @RequestBody Map<String, Object> request);

    /**
     * 获取资源详情 (用于审核展示)
     *
     * @param resourceId 资源ID
     * @return 资源信息
     */
    @GetMapping("/{resourceId}/info")
    Result<Map<String, Object>> getResourceInfo(@PathVariable("resourceId") Long resourceId);
}

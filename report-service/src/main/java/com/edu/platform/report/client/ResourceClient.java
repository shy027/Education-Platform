package com.edu.platform.report.client;

import com.edu.platform.common.result.Result;
import com.edu.platform.report.dto.ResourceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 资源服务Feign客户端
 */
@FeignClient(
    name = "resource-service", 
    url = "${app.feign.services.resource-service.url:http://localhost:8082}", 
    path = "/internal/resource"
)
public interface ResourceClient {

    /**
     * 批量获取资源信息(含标签)
     */
    @PostMapping("/batch")
    Result<List<ResourceResponse>> getResourcesByIds(@RequestBody List<Long> resourceIds);

    /**
     * 获取资源看板统计数据
     */
    @GetMapping("/stats")
    Result<java.util.Map<String, Object>> getStats();
}

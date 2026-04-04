package com.edu.platform.course.client;

import com.edu.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 资源服务 Feign 客户端
 */
@FeignClient(name = "resource-service", url = "${app.feign.services.resource-service.url:http://localhost:8082}", path = "/internal/resource")
public interface ResourceClient {

    /**
     * 批量获取资源信息
     */
    @PostMapping("/batch")
    Result<List<Map<String, Object>>> getResourcesByIds(@RequestBody List<Long> resourceIds);
}

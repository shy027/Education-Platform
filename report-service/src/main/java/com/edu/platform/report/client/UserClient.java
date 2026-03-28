package com.edu.platform.report.client;

import com.edu.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "user-service", path = "/internal/user")
public interface UserClient {

    /**
     * 获取用户看板统计数据
     */
    @GetMapping("/stats")
    Result<Map<String, Object>> getStats();
}

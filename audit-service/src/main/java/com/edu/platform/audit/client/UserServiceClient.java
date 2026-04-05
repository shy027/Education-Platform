package com.edu.platform.audit.client;

import com.edu.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "user-service", url = "${app.feign.services.user-service.url:http://localhost:8081}", path = "/internal/user")
public interface UserServiceClient {

    /**
     * 批量获取用户信息
     */
    @PostMapping("/batch")
    Result<Map<Long, Map<String, Object>>> batchGetUserInfo(@RequestBody List<Long> userIds);
}

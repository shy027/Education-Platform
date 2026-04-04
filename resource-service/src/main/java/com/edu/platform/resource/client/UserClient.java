package com.edu.platform.resource.client;

import com.edu.platform.common.result.Result;
import com.edu.platform.resource.dto.feign.UserManageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 用户服务 Feign 客户端
 *
 * @author Education Platform
 */
@FeignClient(
    name = "user-service", 
    contextId = "resourceUserClient", 
    url = "${app.feign.services.user-service.url:http://localhost:8081}", 
    path = "/internal/user"
)
public interface UserClient {

    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户信息映射表 (ID -> UserManageDTO)
     */
    @PostMapping("/batch")
    Result<Map<Long, UserManageDTO>> batchGetUserInfo(@RequestBody List<Long> userIds);
}

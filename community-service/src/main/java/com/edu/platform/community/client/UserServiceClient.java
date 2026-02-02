package com.edu.platform.community.client;

import com.edu.platform.common.result.Result;
import com.edu.platform.community.dto.response.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 用户服务Feign客户端
 *
 * @author Education Platform
 */
@FeignClient(name = "user-service", path = "/api/v1/users/manage")
public interface UserServiceClient {
    
    /**
     * 批量获取用户信息
     */
    @PostMapping("/batch")
    Result<Map<Long, UserInfoDTO>> batchGetUserInfo(@RequestBody List<Long> userIds);
    
}

package com.edu.platform.course.client;

import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
     * 获取单个用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    Result<UserInfoDTO> getUserInfo(@PathVariable("userId") Long userId);
    
    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户信息Map，key为userId
     */
    @PostMapping("/batch")
    Result<Map<Long, UserInfoDTO>> batchGetUserInfo(@RequestBody List<Long> userIds);
}

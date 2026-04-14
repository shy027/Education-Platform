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
@FeignClient(name = "user-service", path = "/internal/user")
public interface UserServiceClient {
    
    /**
     * 获取单个用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    Result<UserInfoDTO> getUserInfo(@PathVariable("userId") Long userId);
    
    @PostMapping("/batch")
    Result<Map<Long, UserInfoDTO>> batchGetUserInfo(@RequestBody List<Long> userIds);

    /**
     * 根据条件查询用户ID列表
     * @param params department, className
     * @return 用户ID列表
     */
    @PostMapping("/search-ids")
    Result<List<Long>> searchUserIds(@RequestBody Map<String, String> params);

    /**
     * 根据用户ID列表获取去重后的学院和班级选项
     * @param userIds 用户ID列表
     * @return 包含departments和classNames的Map
     */
    @PostMapping("/filter-options")
    Result<Map<String, List<String>>> getMemberFilterOptions(@RequestBody List<Long> userIds);
}

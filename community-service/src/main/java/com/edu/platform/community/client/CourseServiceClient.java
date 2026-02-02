package com.edu.platform.community.client;

import com.edu.platform.common.result.Result;
import com.edu.platform.community.dto.response.CourseMemberDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 课程服务Feign客户端
 *
 * @author Education Platform
 */
@FeignClient(name = "course-service", path = "/api/v1/courses")
public interface CourseServiceClient {
    
    /**
     * 检查用户是否为课程成员
     */
    @GetMapping("/{courseId}/members/check")
    Result<CourseMemberDTO> checkCourseMember(
            @PathVariable("courseId") Long courseId,
            @RequestParam("userId") Long userId
    );
    
}

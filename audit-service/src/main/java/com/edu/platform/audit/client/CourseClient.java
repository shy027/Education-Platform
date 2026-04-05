package com.edu.platform.audit.client;


import com.edu.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 课程服务Feign客户端
 *
 * @author Education Platform
 */
@FeignClient(name = "course-service", url = "${app.feign.services.course-service.url:http://localhost:8083}", path = "/internal/course")
public interface CourseClient {
    
    /**
     * 更新课程审核状态 (内部回调)
     *
     * @param courseId 课程ID
     * @param request 审核状态请求 (auditStatus, auditorId)
     * @return 结果
     */
    @PutMapping("/{courseId}/audit-status")
    Result<Void> updateAuditStatus(
        @PathVariable("courseId") Long courseId,
        @RequestBody Map<String, Object> request
    );
    
    /**
     * 获取课程信息 (用于审核展示)
     *
     * @param courseId 课程ID
     * @return 课程信息
     */
    @GetMapping("/{courseId}/info")
    Result<Map<String, Object>> getCourseInfo(@PathVariable("courseId") Long courseId);

    @GetMapping("/stats")
    Result<Map<String, Object>> getStats();
}

package com.edu.platform.audit.client;

import com.edu.platform.audit.dto.feign.CoursewareInfoDTO;
import com.edu.platform.audit.dto.feign.UpdateAuditStatusRequest;
import com.edu.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 课程服务Feign客户端
 *
 * @author Education Platform
 */
@FeignClient(name = "course-service", url = "http://localhost:8082", path = "/internal/courseware")
public interface CourseClient {
    
    /**
     * 更新课件审核状态
     *
     * @param coursewareId 课件ID
     * @param request 审核状态请求
     * @return 结果
     */
    @PutMapping("/{coursewareId}/audit-status")
    Result<Void> updateAuditStatus(
        @PathVariable("coursewareId") Long coursewareId,
        @RequestBody UpdateAuditStatusRequest request
    );
    
    /**
     * 获取课件信息
     *
     * @param coursewareId 课件ID
     * @return 课件信息
     */
    @GetMapping("/{coursewareId}")
    Result<CoursewareInfoDTO> getCoursewareInfo(@PathVariable("coursewareId") Long coursewareId);
    
}

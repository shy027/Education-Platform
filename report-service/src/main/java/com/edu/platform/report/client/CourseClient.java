package com.edu.platform.report.client;

import com.edu.platform.common.dto.CourseScoringDTO;
import com.edu.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 课程服务内部接口客户端
 */
@FeignClient(name = "course-service", path = "/internal/course")
public interface CourseClient {

    /**
     * 获取课程详情(含维度权重)
     */
    @GetMapping("/{id}")
    Result<CourseScoringDTO> getCourseDetail(@PathVariable("id") Long id);

    /**
     * 获取课程看板统计数据
     */
    @GetMapping("/stats")
    Result<java.util.Map<String, Object>> getStats();
}

package com.edu.platform.ai.client;

import com.edu.platform.common.result.Result;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "course-service")
public interface CourseClient {

    @GetMapping("/api/v1/courses/{id}")
    Result<CourseDetailDTO> getCourseDetail(@PathVariable("id") Long id);

    @GetMapping("/api/v1/courses/{courseId}/coursewares")
    Result<com.edu.platform.common.result.PageResult<CoursewareResponse>> getCoursewareList(
            @PathVariable("courseId") Long courseId,
            @org.springframework.web.bind.annotation.RequestParam(value = "chapterId", required = false) Long chapterId,
            @org.springframework.web.bind.annotation.RequestParam(value = "wareType", required = false) Integer wareType,
            @org.springframework.web.bind.annotation.RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize
    );

    @Data
    class CourseDetailDTO {
        private Long id;
        private String courseName;
        private String courseIntro;
        private String subjectArea;
        private String suggestedDimensions;
        private String keywords;
    }

    @Data
    class CoursewareResponse {
        private Long id;
        private String wareTitle;
        private Integer wareType;
        private String fileUrl;
    }
}

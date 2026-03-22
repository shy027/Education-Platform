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

    @Data
    class CourseDetailDTO {
        private Long id;
        private String courseName;
        private String courseIntro;
        private String subjectArea;
        private String suggestedDimensions;
        private String keywords;
    }
}

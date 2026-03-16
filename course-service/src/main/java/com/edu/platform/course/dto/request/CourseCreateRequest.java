package com.edu.platform.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建课程请求
 */
@Data
public class CourseCreateRequest {

    private String courseName;
    
    private String courseCover;
    
    private String courseIntro;
    
    private String subjectArea;
    
    private Long schoolId;
    
    /**
     * 加入方式:1公开,2需审批
     */
    private Integer joinType = 1;
    
    private Integer maxStudents;
    
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 维度权重配置 (JSON)
     */
    private String dimensionWeights;

    /**
     * 评分构成配置 (JSON)
     */
    private String scoringConfig;
}

package com.edu.platform.course.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 更新课程请求
 */
@Data
public class CourseUpdateRequest {

    private Long id;

    private String courseName;
    
    private String courseCover;
    
    private String courseIntro;
    
    private String subjectArea;
    
    private Integer joinType;
    
    private Integer maxStudents;
    
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
}

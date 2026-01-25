package com.edu.platform.course.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程列表响应
 */
@Data
public class CourseListResponse {

    private Long id;
    
    private String courseCode;
    
    private String courseName;
    
    private String courseCover;
    
    private String subjectArea;
    
    private String schoolName;
    
    private String teacherName;
    
    private Integer joinType;
    
    private Integer studentCount;
    
    private Integer status;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
}

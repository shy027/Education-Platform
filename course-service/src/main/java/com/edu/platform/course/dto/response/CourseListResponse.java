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
    
    /** 课程封面（同 courseCover，前端兼容字段） */
    private String cover;
    
    /** 课程简介（同 courseIntro，前端兼容字段） */
    private String description;
    
    private String subjectArea;
    
    private String schoolName;
    
    private Long teacherId;
    
    private String teacherName;
    
    private Integer joinType;
    
    private Integer memberCount;
    
    private Integer studentCount;
    
    private Integer status;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer auditStatus;

    /** 课程创建时间 */
    private LocalDateTime createdTime;
}

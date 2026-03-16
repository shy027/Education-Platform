package com.edu.platform.course.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程详情响应
 */
@Data
public class CourseDetailResponse {

    private Long id;
    
    private String courseCode;
    
    private String courseName;
    
    private String courseCover;
    
    private String courseIntro;
    
    private String subjectArea;
    
    private Long schoolId;
    private String schoolName; // 需要填充
    
    private Long teacherId;
    private String teacherName; // 需要填充
    private String teacherAvatar; // 需要填充
    
    private Integer joinType;
    
    private Integer maxStudents;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer studentCount;
    
    private Integer coursewareCount;
    
    private Integer taskCount;
    
    private Integer discussionCount;
    
    private Integer auditStatus;
    private LocalDateTime auditTime;
    private Long auditorId;
    private String auditRemark;
    
    private Integer status;
    
    private LocalDateTime createdTime;

    /**
     * 维度权重配置 (JSON)
     */
    private String dimensionWeights;

    /**
     * 评分构成配置 (JSON)
     */
    private String scoringConfig;

    /**
     * 维度定义锁定:0否,1是
     */
    private Integer isDimensionLocked;
}

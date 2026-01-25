package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课程信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_info")
public class Course extends BaseEntity {

    @com.baomidou.mybatisplus.annotation.TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;


    private String courseCode;
    
    private String courseName;
    
    private String courseCover;
    
    private String courseIntro;
    
    private String subjectArea;
    
    private Long schoolId;
    
    private Long teacherId;
    
    /**
     * 加入方式:1公开,2需审批
     */
    private Integer joinType;
    
    private Integer maxStudents;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer studentCount;
    
    private Integer coursewareCount;
    
    private Integer taskCount;
    
    private Integer discussionCount;
    
    /**
     * 审核状态:0待审核,1通过,2拒绝
     */
    private Integer auditStatus;
    
    private LocalDateTime auditTime;
    
    private Long auditorId;
    
    private String auditRemark;
    
    /**
     * 状态:0关闭,1开放,2归档
     */
    private Integer status;
}

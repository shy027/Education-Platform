package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_task")
public class CourseTask extends BaseEntity {

    @com.baomidou.mybatisplus.annotation.TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;


    private Long courseId;
    
    private String taskTitle;
    
    private String taskDescription;
    
    /**
     * 类型:1作业,2测验,3考试
     */
    private Integer taskType;
    
    private BigDecimal totalScore;
    
    private BigDecimal passScore;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer durationMinutes;
    
    private Integer allowRetry;
    
    private Integer maxRetryTimes;
    
    private Integer showAnswer;
    
    private Integer randomQuestion;
    
    private Integer submitCount;
    
    private Long creatorId;
    
    /**
     * 状态:0草稿,1发布,2关闭
     */
    private Integer status;
}

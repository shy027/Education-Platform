package com.edu.platform.course.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务响应
 */
@Data
public class TaskResponse {

    /**
     * 任务ID
     */
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 任务标题
     */
    private String taskTitle;
    
    /**
     * 任务描述
     */
    private String taskDescription;
    
    /**
     * 任务类型 (1作业 2测验 3考试)
     */
    private Integer taskType;
    
    /**
     * 任务类型名称
     */
    private String taskTypeName;
    
    /**
     * 总分
     */
    private BigDecimal totalScore;
    
    /**
     * 及格分
     */
    private BigDecimal passScore;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 时长（分钟）
     */
    private Integer durationMinutes;
    
    /**
     * 是否允许重做
     */
    private Integer allowRetry;
    
    /**
     * 最大重做次数
     */
    private Integer maxRetryTimes;
    
    /**
     * 是否显示答案
     */
    private Integer showAnswer;
    
    /**
     * 提交次数
     */
    private Integer submitCount;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 创建者姓名
     */
    private String creatorName;
    
    /**
     * 状态 (0草稿 1发布 2关闭)
     */
    private Integer status;
    
    /**
     * 状态名称
     */
    private String statusName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}

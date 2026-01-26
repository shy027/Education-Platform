package com.edu.platform.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建任务请求
 */
@Data
public class TaskCreateRequest {

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    /**
     * 任务标题
     */
    @NotBlank(message = "任务标题不能为空")
    private String taskTitle;
    
    /**
     * 任务描述
     */
    private String taskDescription;
    
    /**
     * 任务类型 (1作业 2测验 3考试)
     */
    @NotNull(message = "任务类型不能为空")
    private Integer taskType;
    
    /**
     * 总分
     */
    @NotNull(message = "总分不能为空")
    private BigDecimal totalScore;
    
    /**
     * 及格分
     */
    private BigDecimal passScore;
    
    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    /**
     * 结束时间（可选，为空则任务不会截止）
     */
    private LocalDateTime endTime;
    
    /**
     * 时长（分钟）
     */
    private Integer durationMinutes;
    
    /**
     * 是否允许重做 (0否 1是)
     */
    private Integer allowRetry = 0;
    
    /**
     * 最大重做次数
     */
    private Integer maxRetryTimes = 0;
    
    /**
     * 是否显示答案 (0否 1是)
     */
    private Integer showAnswer = 0;
    
    /**
     * 是否随机题目 (0否 1是)
     */
    private Integer randomQuestion = 0;
}

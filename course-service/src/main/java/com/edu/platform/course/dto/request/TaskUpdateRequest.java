package com.edu.platform.course.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新任务请求
 */
@Data
public class TaskUpdateRequest {

    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long id;
    
    /**
     * 任务标题
     */
    private String taskTitle;
    
    /**
     * 任务描述
     */
    private String taskDescription;
    
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
     * 结束时间（可选，为空则任务不会截止）
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
     * 是否随机题目
     */
    private Integer randomQuestion;
}

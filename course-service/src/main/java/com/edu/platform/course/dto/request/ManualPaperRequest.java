package com.edu.platform.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 手动组卷请求
 */
@Data
@Schema(description = "手动组卷请求")
public class ManualPaperRequest {

    @Schema(description = "任务ID (新创建时传空)", required = false)
    private Long taskId;

    @Schema(description = "课程ID (新创建时必传)", required = false)
    private Long courseId;

    @Schema(description = "任务标题", required = false)
    private String taskTitle;

    @Schema(description = "任务描述", required = false)
    private String taskDescription;

    @Schema(description = "开始时间", required = false)
    private java.time.LocalDateTime startTime;

    @Schema(description = "结束时间", required = false)
    private java.time.LocalDateTime endTime;

    @Schema(description = "时长（分钟）", required = false)
    private Integer durationMinutes;

    @Schema(description = "题目列表", required = true)
    private List<QuestionItem> questions;

    @Data
    @Schema(description = "题目项")
    public static class QuestionItem {
        
        @Schema(description = "题目ID", required = true)
        private Long questionId;
        
        @Schema(description = "该题分值", required = true, example = "10.0")
        private BigDecimal score;
        
        @Schema(description = "排序", example = "1")
        private Integer sortOrder;
    }
}

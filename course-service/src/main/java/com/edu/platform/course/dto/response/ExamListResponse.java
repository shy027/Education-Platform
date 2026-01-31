package com.edu.platform.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试列表响应
 */
@Data
@Schema(description = "考试列表响应")
public class ExamListResponse {

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "考试标题")
    private String title;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "总分")
    private BigDecimal totalScore;

    @Schema(description = "题目数量")
    private Integer questionCount;

    @Schema(description = "考试状态: 0-未开始, 1-进行中, 2-已结束")
    private Integer examStatus;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "时长(分钟)")
    private Integer duration;

    @Schema(description = "学生答题状态: 0-未开始, 1-答题中, 2-已提交")
    private Integer studentStatus;

    @Schema(description = "学生得分")
    private BigDecimal studentScore;
}

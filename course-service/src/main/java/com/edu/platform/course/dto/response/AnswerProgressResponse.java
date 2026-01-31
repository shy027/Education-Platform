package com.edu.platform.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 答题进度响应
 */
@Data
@Schema(description = "答题进度响应")
public class AnswerProgressResponse {

    @Schema(description = "答题记录ID")
    private Long recordId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "考试标题")
    private String examTitle;

    @Schema(description = "答题状态: 0-进行中, 1-已提交, 2-已批改")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "总得分")
    private BigDecimal totalScore;

    @Schema(description = "已答题目数")
    private Integer answeredCount;

    @Schema(description = "总题目数")
    private Integer totalCount;

    @Schema(description = "答案映射 (key: questionId, value: userAnswer)")
    private Map<Long, String> answers;

    @Schema(description = "试卷详情")
    private PaperResponse paper;
}

package com.edu.platform.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 批改结果响应
 */
@Data
@Schema(description = "批改结果响应")
public class GradingResultResponse {

    @Schema(description = "答题记录ID")
    private Long recordId;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "总得分")
    private BigDecimal totalScore;

    @Schema(description = "试卷实际总分")
    private BigDecimal taskTotalScore;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "批改状态: 0-未批改, 1-部分批改, 2-已完成")
    private Integer gradingStatus;

    @Schema(description = "待批改题目数")
    private Integer pendingCount;

    @Schema(description = "是否显示答案: 0-否, 1-是")
    private Integer showAnswer;

    @Schema(description = "答题详情")
    private List<AnswerDetailVO> answers;

    @Data
    @Schema(description = "答题详情")
    public static class AnswerDetailVO {
        
        @Schema(description = "答案ID")
        private Long answerId;
        
        @Schema(description = "试卷题目ID")
        private Long taskQuestionId;
        
        @Schema(description = "题目ID (题库ID)")
        private Long questionId;
        
        @Schema(description = "题目内容")
        private String questionContent;
        
        @Schema(description = "题型")
        private Integer questionType;
        
        @Schema(description = "学生答案")
        private String userAnswer;
        
        @Schema(description = "正确答案")
        private String correctAnswer;
        
        @Schema(description = "得分")
        private BigDecimal score;
        
        @Schema(description = "满分/总分")
        private BigDecimal fullScore;
        
        @Schema(description = "是否正确")
        private Boolean isCorrect;
        
        @Schema(description = "教师评语")
        private String comment;
    }
}

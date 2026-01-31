package com.edu.platform.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷响应
 */
@Data
@Schema(description = "试卷响应")
public class PaperResponse {

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "试卷标题")
    private String title;

    @Schema(description = "总分")
    private BigDecimal totalScore;

    @Schema(description = "题目数量")
    private Integer questionCount;

    @Schema(description = "题目列表")
    private List<PaperQuestionVO> questions;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Data
    @Schema(description = "试卷题目")
    public static class PaperQuestionVO {
        
        @Schema(description = "题目ID")
        private Long questionId;
        
        @Schema(description = "题目内容")
        private String content;
        
        @Schema(description = "题型")
        private Integer questionType;
        
        @Schema(description = "题型名称")
        private String typeName;
        
        @Schema(description = "分值")
        private BigDecimal score;
        
        @Schema(description = "排序")
        private Integer sortOrder;
        
        @Schema(description = "选项列表")
        private List<OptionVO> options;
    }

    @Data
    @Schema(description = "题目选项")
    public static class OptionVO {
        
        @Schema(description = "选项ID")
        private Long id;
        
        @Schema(description = "选项标签")
        private String optionLabel;
        
        @Schema(description = "选项内容")
        private String content;
        
        @Schema(description = "是否正确")
        private Boolean isCorrect;
        
        @Schema(description = "排序")
        private Integer sortOrder;
    }
}

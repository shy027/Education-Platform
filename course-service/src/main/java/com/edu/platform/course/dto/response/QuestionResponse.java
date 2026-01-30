package com.edu.platform.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 题目响应
 */
@Data
@Schema(description = "题目响应")
public class QuestionResponse {

    @Schema(description = "题目ID")
    private Long id;

    @Schema(description = "所属课程ID")
    private Long courseId;

    @Schema(description = "关联章节ID")
    private Long chapterId;

    @Schema(description = "题目内容")
    private String content;

    @Schema(description = "题型: 1-单选 2-多选 3-判断 4-填空 5-简答 6-编程")
    private Integer questionType;

    @Schema(description = "题型名称")
    private String typeName;

    @Schema(description = "分数")
    private Double score;

    @Schema(description = "正确答案 (填空题)")
    private String correctAnswer;

    @Schema(description = "参考答案 (简答题、编程题)")
    private String referenceAnswer;

    @Schema(description = "题目解析")
    private String analysis;

    @Schema(description = "难度: 1-简单 2-中等 3-困难 4-很难 5-极难")
    private Integer difficulty;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "状态:0禁用,1正常")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "选项列表")
    private List<QuestionOptionVO> options;

    @Schema(description = "维度关联 (维度名称 -> 权重)")
    private Map<String, BigDecimal> dimensions;

    @Data
    @Schema(description = "题目选项")
    public static class QuestionOptionVO {
        @Schema(description = "选项ID")
        private Long id;

        @Schema(description = "选项标签")
        private String optionLabel;

        @Schema(description = "选项内容")
        private String content;

        @Schema(description = "是否正确答案")
        private Boolean isCorrect;

        @Schema(description = "排序")
        private Integer sortOrder;
    }
}

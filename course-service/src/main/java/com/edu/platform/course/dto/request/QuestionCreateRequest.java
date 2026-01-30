package com.edu.platform.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 创建题目请求
 */
@Data
@Schema(description = "创建题目请求")
public class QuestionCreateRequest {

    @Schema(description = "所属课程ID (0表示公共题库)")
    private Long courseId;

    @Schema(description = "题目类型: 1-单选 2-多选 3-判断 4-填空 5-简答 6-编程")
    private Integer questionType;

    @Schema(description = "题目内容")
    private String content;

    @Schema(description = "分数")
    private Double score;

    @Schema(description = "难度: 1-简单 2-中等 3-困难 4-很难 5-极难")
    private Integer difficulty;

    @Schema(description = "选项列表 (选择题和判断题需要)")
    private List<QuestionOptionDTO> options;

    @Schema(description = "正确答案 (填空题需要)")
    private String correctAnswer;

    @Schema(description = "参考答案 (简答题和编程题需要)")
    private String referenceAnswer;

    @Schema(description = "题目解析")
    private String analysis;

    @Schema(description = "关联的能力维度ID列表")
    private List<Long> dimensionIds;

    @Data
    @Schema(description = "题目选项")
    public static class QuestionOptionDTO {
        @Schema(description = "选项标签 (A, B, C, D)")
        private String optionLabel;

        @Schema(description = "选项内容")
        private String content;

        @Schema(description = "是否正确答案")
        private Boolean isCorrect;

        @Schema(description = "排序")
        private Integer sortOrder;
    }
}

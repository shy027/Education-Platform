package com.edu.platform.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 更新题目请求
 */
@Data
@Schema(description = "更新题目请求")
public class QuestionUpdateRequest {

    @Schema(description = "题目内容")
    private String content;

    @Schema(description = "分数")
    private Double score;

    @Schema(description = "难度: 1-简单 2-中等 3-困难 4-很难 5-极难")
    private Integer difficulty;

    @Schema(description = "选项列表 (选择题和判断题需要)")
    private List<QuestionCreateRequest.QuestionOptionDTO> options;

    @Schema(description = "正确答案 (填空题需要)")
    private String correctAnswer;

    @Schema(description = "参考答案 (简答题和编程题需要)")
    private String referenceAnswer;

    @Schema(description = "题目解析")
    private String analysis;

    @Schema(description = "关联的能力维度ID列表")
    private List<Long> dimensionIds;
}

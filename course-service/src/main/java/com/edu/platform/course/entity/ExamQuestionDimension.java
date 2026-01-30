package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 题目维度关联表
 */
@Data
@TableName("exam_question_dimension")
public class ExamQuestionDimension {

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 维度ID
     */
    private Long dimensionId;

    /**
     * 权重
     */
    private BigDecimal weight;
}

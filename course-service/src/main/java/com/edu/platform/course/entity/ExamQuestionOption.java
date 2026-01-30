package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目选项表
 */
@Data
@TableName("exam_question_option")
public class ExamQuestionOption {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 选项标签 (A, B, C, D)
     */
    private String optionLabel;

    /**
     * 选项内容
     */
    private String content;

    /**
     * 是否正确答案:0否,1是
     */
    private Integer isCorrect;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}

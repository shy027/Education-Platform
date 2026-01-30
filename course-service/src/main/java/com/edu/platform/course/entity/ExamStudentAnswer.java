package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生答题明细表
 */
@Data
@TableName("exam_student_answer")
public class ExamStudentAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 记录ID
     */
    private Long recordId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 学生答案
     */
    private String userAnswer;

    /**
     * 获得分数
     */
    private BigDecimal score;

    /**
     * 是否正确:0否,1是
     */
    private Integer isCorrect;

    /**
     * 教师评语
     */
    private String comment;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}

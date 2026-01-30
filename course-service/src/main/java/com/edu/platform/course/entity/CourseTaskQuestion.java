package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 任务题目关联表(试卷结构)
 */
@Data
@TableName("course_task_question")
public class CourseTaskQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 该题分值
     */
    private BigDecimal score;

    /**
     * 题目顺序
     */
    private Integer sortOrder;
}

package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 能力维度表
 * 全局维度,由管理员统一创建和管理
 */
@Data
@TableName("exam_dimension")
public class ExamDimension {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 维度名称
     */
    private String name;

    /**
     * 维度描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 逻辑删除
     */
    private Integer isDeleted;
}

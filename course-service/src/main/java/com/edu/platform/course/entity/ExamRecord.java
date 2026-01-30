package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 做题记录表
 */
@Data
@TableName("exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 学生ID
     */
    private Long userId;

    /**
     * 总得分
     */
    private BigDecimal totalScore;

    /**
     * 状态:0进行中,1已提交,2已批改
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 批改人ID
     */
    private Long graderId;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}

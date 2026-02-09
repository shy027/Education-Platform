package com.edu.platform.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 素养画像历史实体
 *
 * @author Education Platform
 */
@Data
@TableName("report_profile_history")
public class ProfileHistory implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    private Long userId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 维度1得分
     */
    private BigDecimal dimension1Score;
    
    /**
     * 维度2得分
     */
    private BigDecimal dimension2Score;
    
    /**
     * 维度3得分
     */
    private BigDecimal dimension3Score;
    
    /**
     * 维度4得分
     */
    private BigDecimal dimension4Score;
    
    /**
     * 维度5得分
     */
    private BigDecimal dimension5Score;
    
    /**
     * 综合得分
     */
    private BigDecimal totalScore;
    
    /**
     * 快照日期
     */
    private LocalDate snapshotDate;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
}

package com.edu.platform.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生思政素养画像实体
 *
 * @author Education Platform
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report_student_profile")
public class StudentProfile extends BaseEntity {
    
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
     * 维度1得分(价值观认同)
     */
    private BigDecimal dimension1Score;
    
    /**
     * 维度2得分(思想品德)
     */
    private BigDecimal dimension2Score;
    
    /**
     * 维度3得分(社会责任)
     */
    private BigDecimal dimension3Score;
    
    /**
     * 维度4得分(创新精神)
     */
    private BigDecimal dimension4Score;
    
    /**
     * 维度5得分(团队协作)
     */
    private BigDecimal dimension5Score;
    
    /**
     * 综合得分
     */
    private BigDecimal totalScore;
    
    /**
     * 等级:优秀/良好/合格/待提升
     */
    private String level;
    
    /**
     * 成长趋势:上升/稳定/下降
     */
    private String growthTrend;
    
    /**
     * 最后计算时间
     */
    private LocalDateTime lastCalcTime;
    
}

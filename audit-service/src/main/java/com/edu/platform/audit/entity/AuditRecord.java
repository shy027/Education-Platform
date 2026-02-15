package com.edu.platform.audit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 审核记录实体
 *
 * @author Education Platform
 */
@Data
@TableName("audit_record")
public class AuditRecord {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 内容类型: COURSEWARE/POST/COMMENT
     */
    private String contentType;
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 审核方式: 1-AI, 2-人工
     */
    private Integer auditMethod;
    
    /**
     * 审核结果: 0-待审核, 1-通过, 2-拒绝
     */
    private Integer auditResult;
    
    /**
     * 审核原因
     */
    private String auditReason;
    
    /**
     * 风险等级: 1-低, 2-中, 3-高
     */
    private Integer riskLevel;
    
    /**
     * AI置信度 (0-100)
     */
    private BigDecimal aiConfidence;
    
    /**
     * 审核人ID
     */
    private Long auditorId;
    
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}

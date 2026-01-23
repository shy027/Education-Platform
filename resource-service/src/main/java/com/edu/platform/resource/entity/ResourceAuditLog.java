package com.edu.platform.resource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源审核记录表
 *
 * @author Education Platform
 */
@Data
@TableName("resource_audit_log")
public class ResourceAuditLog {
    
    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 资源ID
     */
    private Long resourceId;
    
    /**
     * 审核人ID
     */
    private Long auditorId;
    
    /**
     * 审核结果: 1-通过, 2-拒绝
     */
    private Integer auditResult;
    
    /**
     * 审核备注
     */
    private String auditRemark;
    
    /**
     * 审核时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime auditTime;
    
}

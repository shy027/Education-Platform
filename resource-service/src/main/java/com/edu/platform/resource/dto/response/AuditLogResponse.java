package com.edu.platform.resource.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核记录响应DTO
 *
 * @author Education Platform
 */
@Data
public class AuditLogResponse {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 审核人ID
     */
    private Long auditorId;
    
    /**
     * 审核人名称
     */
    private String auditorName;
    
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
    private LocalDateTime auditTime;
    
}

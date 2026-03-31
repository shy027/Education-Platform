package com.edu.platform.resource.dto.client;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 审核记录视图对象 (Feign)
 */
@Data
public class AuditRecordVO {
    private Long id;
    private String contentType;
    private Long contentId;
    private Integer auditMethod;
    private Integer auditResult;
    private String auditReason;
    private Integer riskLevel;
    private BigDecimal aiConfidence;
    private Long auditorId;
    private LocalDateTime auditTime;
    private LocalDateTime createdTime;
}

package com.edu.platform.resource.dto.client;

import lombok.Builder;
import lombok.Data;

/**
 * 手动审核上报请求 (Feign)
 */
@Data
@Builder
public class ManualAuditRecordRequest {
    private String contentType;
    private Long contentId;
    private Integer auditResult;
    private String auditReason;
    private Long auditorId;
}

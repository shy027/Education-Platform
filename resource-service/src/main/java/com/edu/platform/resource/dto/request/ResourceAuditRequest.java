package com.edu.platform.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 资源审核请求DTO
 *
 * @author Education Platform
 */
@Data
public class ResourceAuditRequest {
    
    /**
     * 审核结果: 1-通过, 2-拒绝
     */
    @NotNull(message = "审核结果不能为空")
    private Integer auditResult;
    
    /**
     * 审核备注
     */
    private String auditRemark;
    
}

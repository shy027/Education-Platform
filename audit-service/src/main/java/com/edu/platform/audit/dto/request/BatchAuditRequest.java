package com.edu.platform.audit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量审核请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "批量审核请求")
public class BatchAuditRequest {
    
    @Schema(description = "审核记录ID列表", required = true)
    @NotEmpty(message = "审核记录ID列表不能为空")
    private List<Long> recordIds;
    
    @Schema(description = "审核结果: 1-通过, 2-拒绝", required = true)
    @NotNull(message = "审核结果不能为空")
    private Integer auditResult;
    
    @Schema(description = "审核原因")
    private String auditReason;
}

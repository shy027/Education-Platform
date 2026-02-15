package com.edu.platform.community.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新审核状态请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "更新审核状态请求")
public class UpdateAuditStatusRequest {
    
    @Schema(description = "审核状态: 1-通过, 2-拒绝")
    private Integer auditStatus;
    
}

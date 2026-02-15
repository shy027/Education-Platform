package com.edu.platform.audit.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批量审核结果
 *
 * @author Education Platform
 */
@Data
@Schema(description = "批量审核结果")
public class BatchAuditResult {
    
    @Schema(description = "成功数量")
    private Integer successCount;
    
    @Schema(description = "失败数量")
    private Integer failCount;
    
    public BatchAuditResult() {
        this.successCount = 0;
        this.failCount = 0;
    }
    
    public void incrementSuccess() {
        this.successCount++;
    }
    
    public void incrementFail() {
        this.failCount++;
    }
}

package com.edu.platform.audit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 手动审核上报请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "手动审核上报请求")
public class ManualAuditRecordRequest {

    @Schema(description = "内容类型: RESOURCE/COURSEWARE/POST/COMMENT")
    @NotBlank(message = "内容类型不能为空")
    private String contentType;

    @Schema(description = "内容ID")
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    @Schema(description = "审核结果: 1-通过, 2-拒绝")
    @NotNull(message = "审核结果不能为空")
    private Integer auditResult;

    @Schema(description = "审核原因")
    private String auditReason;

    @Schema(description = "审核人ID")
    @NotNull(message = "审核人ID不能为空")
    private Long auditorId;
}

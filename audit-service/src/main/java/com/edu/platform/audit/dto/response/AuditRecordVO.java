package com.edu.platform.audit.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 审核记录响应
 *
 * @author Education Platform
 */
@Data
@Schema(description = "审核记录响应")
public class AuditRecordVO {
    
    @Schema(description = "记录ID")
    private Long recordId;
    
    @Schema(description = "内容类型")
    private String contentType;
    
    @Schema(description = "内容类型名称")
    private String contentTypeName;
    
    @Schema(description = "内容ID")
    private Long contentId;
    
    @Schema(description = "内容标题")
    private String contentTitle;
    
    @Schema(description = "内容预览")
    private String contentPreview;
    
    @Schema(description = "审核方式: 1-AI, 2-人工")
    private Integer auditMethod;
    
    @Schema(description = "审核方式名称")
    private String auditMethodName;
    
    @Schema(description = "审核结果: 0-待审核, 1-通过, 2-拒绝")
    private Integer auditResult;
    
    @Schema(description = "审核结果名称")
    private String auditResultName;
    
    @Schema(description = "审核原因")
    private String auditReason;
    
    @Schema(description = "风险等级: 1-低, 2-中, 3-高")
    private Integer riskLevel;
    
    @Schema(description = "风险等级名称")
    private String riskLevelName;
    
    @Schema(description = "AI置信度")
    private BigDecimal aiConfidence;
    
    @Schema(description = "审核人ID")
    private Long auditorId;
    
    @Schema(description = "审核人姓名")
    private String auditorName;
    
    @Schema(description = "创建者ID")
    private Long creatorId;
    
    @Schema(description = "创建者姓名")
    private String creatorName;
    
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}

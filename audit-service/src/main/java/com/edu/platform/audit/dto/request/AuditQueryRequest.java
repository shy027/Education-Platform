package com.edu.platform.audit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 审核查询请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "审核查询请求")
public class AuditQueryRequest {
    
    @Schema(description = "内容类型: COURSEWARE/POST/COMMENT")
    private String contentType;
    
    @Schema(description = "审核结果: 0-待审核, 1-通过, 2-拒绝")
    private Integer auditResult;
    
    @Schema(description = "风险等级: 1-低, 2-中, 3-高")
    private Integer riskLevel;
    
    @Schema(description = "开始日期")
    private String startDate;
    
    @Schema(description = "结束日期")
    private String endDate;
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}

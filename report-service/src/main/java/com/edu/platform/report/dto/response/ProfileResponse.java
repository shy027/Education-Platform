package com.edu.platform.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 素养画像响应DTO
 *
 * @author Education Platform
 */
@Data
@Schema(description = "素养画像响应")
public class ProfileResponse {
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "维度1得分(学习投入)")
    private BigDecimal dimension1Score;
    
    @Schema(description = "维度2得分(知识掌握)")
    private BigDecimal dimension2Score;
    
    @Schema(description = "维度3得分(思政参与)")
    private BigDecimal dimension3Score;
    
    @Schema(description = "维度4得分(互动协作)")
    private BigDecimal dimension4Score;
    
    @Schema(description = "维度5得分(反思成长)")
    private BigDecimal dimension5Score;
    
    @Schema(description = "综合得分")
    private BigDecimal totalScore;
    
    @Schema(description = "素养等级(优秀/良好/合格/待提升)")
    private String profileLevel;
    
    @Schema(description = "成长趋势(上升/稳定/下降)")
    private String growthTrend;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
    
}

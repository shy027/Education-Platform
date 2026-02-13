package com.edu.platform.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 成长轨迹响应DTO
 *
 * @author Education Platform
 */
@Data
@Schema(description = "成长轨迹响应")
public class GrowthTrackResponse {
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "轨迹数据点列表")
    private List<TrackPoint> trackData;
    
    @Schema(description = "成长趋势")
    private String trend;
    
    @Schema(description = "进步幅度")
    private BigDecimal improvement;
    
    /**
     * 轨迹数据点
     */
    @Data
    @Schema(description = "轨迹数据点")
    public static class TrackPoint {
        
        @Schema(description = "日期")
        private LocalDate date;
        
        @Schema(description = "综合得分")
        private BigDecimal totalScore;
        
        @Schema(description = "维度1得分")
        private BigDecimal dimension1Score;
        
        @Schema(description = "维度2得分")
        private BigDecimal dimension2Score;
        
        @Schema(description = "维度3得分")
        private BigDecimal dimension3Score;
        
        @Schema(description = "维度4得分")
        private BigDecimal dimension4Score;
        
        @Schema(description = "维度5得分")
        private BigDecimal dimension5Score;
    }
}

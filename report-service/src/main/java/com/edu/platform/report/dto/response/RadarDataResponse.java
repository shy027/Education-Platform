package com.edu.platform.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 雷达图数据响应DTO
 *
 * @author Education Platform
 */
@Data
@Schema(description = "雷达图数据响应")
public class RadarDataResponse {
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "五维度数据")
    private List<DimensionData> dimensions;
    
    @Schema(description = "综合得分")
    private BigDecimal totalScore;
    
    @Schema(description = "素养等级")
    private String level;
    
    @Schema(description = "成长趋势")
    private String growthTrend;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
    
    /**
     * 维度数据
     */
    @Data
    @Schema(description = "维度数据")
    public static class DimensionData {
        
        @Schema(description = "维度名称")
        private String name;
        
        @Schema(description = "维度得分")
        private BigDecimal score;
        
        @Schema(description = "最大分值")
        private BigDecimal maxScore;
        
        public DimensionData(String name, BigDecimal score) {
            this.name = name;
            this.score = score;
            this.maxScore = BigDecimal.valueOf(100);
        }
    }
}

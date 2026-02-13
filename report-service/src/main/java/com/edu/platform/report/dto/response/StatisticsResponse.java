package com.edu.platform.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

/**
 * 学习统计响应DTO
 *
 * @author Education Platform
 */
@Data
@Schema(description = "学习统计响应")
public class StatisticsResponse {
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "统计周期")
    private Period period;
    
    @Schema(description = "行为统计数据")
    private Map<String, BehaviorStat> behaviorStats;
    
    @Schema(description = "汇总数据")
    private Summary summary;
    
    /**
     * 统计周期
     */
    @Data
    @Schema(description = "统计周期")
    public static class Period {
        
        @Schema(description = "开始日期")
        private LocalDate startDate;
        
        @Schema(description = "结束日期")
        private LocalDate endDate;
        
        @Schema(description = "天数")
        private Integer days;
    }
    
    /**
     * 行为统计
     */
    @Data
    @Schema(description = "行为统计")
    public static class BehaviorStat {
        
        @Schema(description = "行为次数")
        private Integer count;
        
        @Schema(description = "总时长(秒)")
        private Integer totalDuration;
        
        @Schema(description = "平均得分")
        private Double avgScore;
        
        @Schema(description = "总点赞数")
        private Integer totalLikes;
        
        @Schema(description = "平均长度")
        private Integer avgLength;
        
        @Schema(description = "连续天数")
        private Integer consecutiveDays;
    }
    
    /**
     * 汇总数据
     */
    @Data
    @Schema(description = "汇总数据")
    public static class Summary {
        
        @Schema(description = "总行为次数")
        private Integer totalBehaviors;
        
        @Schema(description = "活跃天数")
        private Integer activeDays;
        
        @Schema(description = "日均行为次数")
        private Double avgDailyBehaviors;
        
        @Schema(description = "最活跃时段")
        private Integer mostActiveHour;
    }
}

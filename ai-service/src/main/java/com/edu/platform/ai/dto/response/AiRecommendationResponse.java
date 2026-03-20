package com.edu.platform.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * AI 资源推荐结果
 */
@Data
public class AiRecommendationResponse {
    
    private List<Recommendation> recommendations;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Recommendation {
        private Long resourceId;
        private String title;
        private String reason;
        private Double matchScore;
    }
}

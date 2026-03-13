package com.edu.platform.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 资源标签权重及封顶配置更新请求
 */
@Data
@Schema(description = "资源标签权重及封顶配置更新请求")
public class TagWeightsUpdateRequest {
    
    @Schema(description = "标签配置Map (Key: 标签名, Value: 配置详情)")
    private Map<String, TagConfig> tagConfigs;

    @Data
    public static class TagConfig {
        @JsonProperty("max_score")
        @Schema(description = "该标签的分数上限", example = "10.0")
        private Double maxScore;
        
        @Schema(description = "六维度权重分配 (Key: dimension1~6, Value: 0~5星级)")
        private Map<String, Double> weights;
    }
}

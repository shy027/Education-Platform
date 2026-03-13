package com.edu.platform.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 评分构成配置更新请求
 */
@Data
@Schema(description = "评分构成配置更新请求")
public class ScoreConfigUpdateRequest {
    
    @JsonProperty("course_cap")
    @Schema(description = "课程得分上限", example = "80")
    private BigDecimal courseCap;
    
    @JsonProperty("resource_cap")
    @Schema(description = "资源浏览得分上限", example = "20")
    private BigDecimal resourceCap;
    
    @JsonProperty("resource_view_point")
    @Schema(description = "单次资源浏览基础分", example = "2.0")
    private BigDecimal resourceViewPoint;
}

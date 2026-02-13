package com.edu.platform.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 权重更新请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "权重更新请求")
public class WeightsUpdateRequest {
    
    @Schema(description = "维度1权重(政治认同)", example = "0.25")
    private BigDecimal dimension_1;
    
    @Schema(description = "维度2权重(家国情怀)", example = "0.25")
    private BigDecimal dimension_2;
    
    @Schema(description = "维度3权重(道德修养)", example = "0.20")
    private BigDecimal dimension_3;
    
    @Schema(description = "维度4权重(法治意识)", example = "0.15")
    private BigDecimal dimension_4;
    
    @Schema(description = "维度5权重(文化素养)", example = "0.15")
    private BigDecimal dimension_5;
}

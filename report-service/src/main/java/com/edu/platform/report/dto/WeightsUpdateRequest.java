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
    
    @Schema(description = "维度1权重", example = "0.15")
    private BigDecimal dimension1;
    
    @Schema(description = "维度2权重", example = "0.20")
    private BigDecimal dimension2;
    
    @Schema(description = "维度3权重", example = "0.15")
    private BigDecimal dimension3;
    
    @Schema(description = "维度4权重", example = "0.15")
    private BigDecimal dimension4;
    
    @Schema(description = "维度5权重", example = "0.15")
    private BigDecimal dimension5;
    
    @Schema(description = "维度6权重", example = "0.20")
    private BigDecimal dimension6;
}

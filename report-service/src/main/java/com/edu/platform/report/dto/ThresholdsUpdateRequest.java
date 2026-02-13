package com.edu.platform.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 阈值更新请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "阈值更新请求")
public class ThresholdsUpdateRequest {
    
    @Schema(description = "优秀阈值", example = "90")
    private BigDecimal excellent;
    
    @Schema(description = "良好阈值", example = "80")
    private BigDecimal good;
    
    @Schema(description = "合格阈值", example = "60")
    private BigDecimal pass;
}

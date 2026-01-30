package com.edu.platform.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 能力维度响应
 */
@Data
@Schema(description = "能力维度响应")
public class DimensionResponse {

    @Schema(description = "维度ID")
    private Long id;

    @Schema(description = "维度名称")
    private String name;

    @Schema(description = "维度描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}

package com.edu.platform.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建能力维度请求
 */
@Data
@Schema(description = "创建能力维度请求")
public class DimensionCreateRequest {

    @Schema(description = "维度名称", example = "政治认同")
    private String name;

    @Schema(description = "维度描述", example = "考察学生对中国特色社会主义的认同程度")
    private String description;
}

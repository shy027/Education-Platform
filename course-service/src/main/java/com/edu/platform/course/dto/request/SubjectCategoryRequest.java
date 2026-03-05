package com.edu.platform.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 学科领域分类请求参数
 */
@Data
public class SubjectCategoryRequest {

    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @NotBlank(message = "学科名称不能为空")
    @Schema(description = "学科名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "排序号不能为空")
    @Schema(description = "排序号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sortOrder;
}

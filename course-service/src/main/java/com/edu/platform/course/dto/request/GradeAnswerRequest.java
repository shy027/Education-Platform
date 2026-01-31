package com.edu.platform.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 批改答案请求
 */
@Data
@Schema(description = "批改答案请求")
public class GradeAnswerRequest {

    @Schema(description = "学生答案ID", required = true)
    private Long answerId;

    @Schema(description = "得分", required = true)
    private BigDecimal score;

    @Schema(description = "教师评语")
    private String comment;
}

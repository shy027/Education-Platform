package com.edu.platform.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 手动组卷请求
 */
@Data
@Schema(description = "手动组卷请求")
public class ManualPaperRequest {

    @Schema(description = "任务ID", required = true)
    private Long taskId;

    @Schema(description = "题目列表", required = true)
    private List<QuestionItem> questions;

    @Data
    @Schema(description = "题目项")
    public static class QuestionItem {
        
        @Schema(description = "题目ID", required = true)
        private Long questionId;
        
        @Schema(description = "该题分值", required = true, example = "10.0")
        private BigDecimal score;
        
        @Schema(description = "排序", example = "1")
        private Integer sortOrder;
    }
}

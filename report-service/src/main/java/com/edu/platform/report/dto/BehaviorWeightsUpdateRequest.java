package com.edu.platform.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 行为基础分值更新请求
 */
@Data
@Schema(description = "行为基础分值更新请求")
public class BehaviorWeightsUpdateRequest {

    @JsonProperty("VIEW_COURSEWARE")
    @Schema(description = "查看课件单次得分")
    private BigDecimal viewCourseware;

    @JsonProperty("SUBMIT_TASK")
    @Schema(description = "提交任务(作业/测验)基础分")
    private BigDecimal submitTask;

    @JsonProperty("POST_COMMENT")
    @Schema(description = "回复讨论基础分")
    private BigDecimal postReply;

    @JsonProperty("ESSENCE_POST")
    @Schema(description = "精华话题额外分")
    private BigDecimal essencePost;
}

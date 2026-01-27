package com.edu.platform.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课件详情响应
 *
 * @author Education Platform
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课件详情响应")
public class CoursewareDetailResponse extends CoursewareResponse {
    
    @Schema(description = "学习进度(秒)")
    private Integer progressSeconds;
    
    @Schema(description = "是否完成: 0-未完成 1-已完成")
    private Integer completed;
    
    @Schema(description = "学习进度百分比")
    private Integer progressPercent;
}

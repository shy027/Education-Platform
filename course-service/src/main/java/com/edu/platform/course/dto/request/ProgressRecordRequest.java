package com.edu.platform.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 学习进度记录请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "学习进度记录请求")
public class ProgressRecordRequest {
    
    @NotNull(message = "进度秒数不能为空")
    @Schema(description = "学习进度(秒)")
    private Integer progressSeconds;
    
    @Schema(description = "是否完成: 0-未完成 1-已完成")
    private Integer completed = 0;
}

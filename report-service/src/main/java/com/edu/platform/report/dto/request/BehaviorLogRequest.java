package com.edu.platform.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 行为埋点请求DTO
 *
 * @author Education Platform
 */
@Data
@Schema(description = "行为埋点请求")
public class BehaviorLogRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "课程ID", example = "1001")
    private Long courseId;
    
    @Schema(description = "行为类型", example = "VIEW_COURSEWARE", required = true)
    private String behaviorType;
    
    @Schema(description = "对象类型", example = "COURSEWARE")
    private String behaviorObjectType;
    
    @Schema(description = "对象ID", example = "2001")
    private Long behaviorObjectId;
    
    @Schema(description = "行为数据(JSON格式)", example = "{\"progress\":50,\"completed\":false}")
    private String behaviorData;
    
    @Schema(description = "持续时长(秒)", example = "300")
    private Integer durationSeconds;
    
}

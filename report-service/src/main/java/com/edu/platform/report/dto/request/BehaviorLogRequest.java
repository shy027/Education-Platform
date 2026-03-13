package com.edu.platform.report.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("behaviorType")
    @Schema(description = "行为类型", example = "VIEW_COURSEWARE", required = true)
    private String behaviorType;
    
    @JsonProperty("behaviorObjectType")
    @Schema(description = "对象类型", example = "COURSEWARE")
    private String behaviorObjectType;
    
    @JsonProperty("behaviorObjectId")
    @JsonAlias("targetId") // 允许从 targetId 映射
    @Schema(description = "对象ID", example = "2001")
    private Long behaviorObjectId;
    
    @JsonProperty("behaviorData")
    @JsonAlias("extra") // 允许从 extra 映射
    @Schema(description = "行为数据(JSON格式)", example = "{\"progress\":50,\"completed\":false}")
    private String behaviorData;
    
    @JsonProperty("durationSeconds")
    @JsonAlias("duration") // 允许从 duration 映射
    @Schema(description = "持续时长(秒)", example = "300")
    private Integer durationSeconds;
    
}

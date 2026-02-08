package com.edu.platform.community.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 话题列表响应
 */
@Data
@Schema(description = "话题列表响应")
public class TopicListResponse {
    
    @Schema(description = "话题ID")
    private Long topicId;
    
    @Schema(description = "小组ID")
    private Long groupId;
    
    @Schema(description = "小组名称")
    private String groupName;
    
    @Schema(description = "创建者ID")
    private Long creatorId;
    
    @Schema(description = "创建者姓名")
    private String creatorName;
    
    @Schema(description = "话题标题")
    private String title;
    
    @Schema(description = "截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime deadline;
    
    @Schema(description = "状态:1进行中,2已结束")
    private Integer status;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdTime;
}

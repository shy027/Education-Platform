package com.edu.platform.community.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 更新话题请求
 */
@Data
@Schema(description = "更新话题请求")
public class UpdateTopicRequest {
    
    @Schema(description = "话题标题")
    private String title;
    
    @Schema(description = "话题内容/要求")
    private String content;
    
    @Schema(description = "截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime deadline;
    
    @Schema(description = "状态:1进行中,2已结束")
    private Integer status;
}

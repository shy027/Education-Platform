package com.edu.platform.community.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建话题请求
 */
@Data
@Schema(description = "创建话题请求")
public class CreateTopicRequest {
    
    @Schema(description = "话题标题")
    @NotBlank(message = "话题标题不能为空")
    private String title;
    
    @Schema(description = "话题内容/要求")
    private String content;
    
    @Schema(description = "截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime deadline;
}

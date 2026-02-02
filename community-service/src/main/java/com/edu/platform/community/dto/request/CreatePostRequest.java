package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建帖子请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "创建帖子请求")
public class CreatePostRequest {
    
    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    @Schema(description = "话题标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "话题标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String postTitle;
    
    @Schema(description = "话题说明(可选,教师可以添加补充说明)")
    @Size(max = 10000, message = "内容长度不能超过10000字符")
    private String postContent;
    
    @Schema(description = "附件URLs(JSON数组字符串)")
    private String attachmentUrls;
    
}

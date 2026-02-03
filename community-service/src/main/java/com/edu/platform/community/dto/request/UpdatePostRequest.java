package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 编辑话题请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "编辑话题请求")
public class UpdatePostRequest {
    
    @NotBlank(message = "标题不能为空")
    @Schema(description = "话题标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String postTitle;
    
    @Schema(description = "话题内容")
    private String postContent;
    
    @Schema(description = "附件URLs,多个URL用逗号分隔")
    private String attachmentUrls;
}

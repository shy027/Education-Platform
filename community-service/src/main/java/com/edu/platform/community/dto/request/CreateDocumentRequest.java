package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建文档请求
 */
@Data
@Schema(description = "创建文档请求")
public class CreateDocumentRequest {
    
    @Schema(description = "文档标题")
    @NotBlank(message = "文档标题不能为空")
    private String title;
    
    @Schema(description = "文档内容(富文本)")
    private String content;
    
    @Schema(description = "关联话题ID(可选)")
    private Long topicId;
}

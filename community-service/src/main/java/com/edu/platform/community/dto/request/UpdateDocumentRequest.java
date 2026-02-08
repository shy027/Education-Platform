package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新文档请求
 */
@Data
@Schema(description = "更新文档请求")
public class UpdateDocumentRequest {
    
    @Schema(description = "文档内容(富文本)")
    @NotBlank(message = "文档内容不能为空")
    private String content;
    
    @Schema(description = "当前版本号(用于乐观锁)")
    @NotNull(message = "版本号不能为空")
    private Integer version;
}

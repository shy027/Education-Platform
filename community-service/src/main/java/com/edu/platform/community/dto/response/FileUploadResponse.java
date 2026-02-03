package com.edu.platform.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件上传响应
 *
 * @author Education Platform
 */
@Data
@Schema(description = "文件上传响应")
public class FileUploadResponse {
    
    @Schema(description = "文件URL")
    private String url;
    
    @Schema(description = "文件名")
    private String fileName;
    
    @Schema(description = "文件大小(字节)")
    private Long fileSize;
}

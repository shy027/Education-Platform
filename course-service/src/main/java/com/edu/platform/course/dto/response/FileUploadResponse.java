package com.edu.platform.course.dto.response;

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
    
    @Schema(description = "文件名")
    private String fileName;
    
    @Schema(description = "文件URL")
    private String fileUrl;
    
    @Schema(description = "文件大小(字节)")
    private Long fileSize;
    
    @Schema(description = "文件类型")
    private String fileType;
    
    @Schema(description = "MIME类型")
    private String mimeType;
    
    @Schema(description = "时长(秒,视频/音频)")
    private Integer duration;
    
    @Schema(description = "页数(PDF)")
    private Integer pageCount;
}

package com.edu.platform.resource.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 资源附件请求DTO
 *
 * @author Education Platform
 */
@Data
@Schema(description = "资源附件请求")
public class ResourceAttachmentRequest {

    @Schema(description = "文件名", example = "course.mp4")
    private String fileName;

    @Schema(description = "文件URL", example = "https://oss...")
    private String fileUrl;

    @Schema(description = "文件大小(字节)", example = "1024000")
    private Long fileSize;

    @Schema(description = "文件类型", example = "mp4")
    private String fileType;

    @Schema(description = "视频时长(秒)", example = "60")
    private Integer duration;

    @Schema(description = "视频缩略图URL")
    private String thumbnailUrl;
    
    @Schema(description = "PDF页数")
    private Integer pageCount;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;
}

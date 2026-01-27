package com.edu.platform.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 课件上传请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "课件上传请求")
public class CoursewareUploadRequest {
    
    @NotNull(message = "章节ID不能为空")
    @Schema(description = "章节ID")
    private Long chapterId;
    
    @NotBlank(message = "课件标题不能为空")
    @Schema(description = "课件标题")
    private String wareTitle;
    
    @NotNull(message = "课件类型不能为空")
    @Schema(description = "课件类型: 1-视频 2-文档 3-音频")
    private Integer wareType;
    
    @NotBlank(message = "文件URL不能为空")
    @Schema(description = "文件URL")
    private String fileUrl;
    
    @Schema(description = "文件大小(字节)")
    private Long fileSize;
    
    @Schema(description = "时长(秒,视频/音频)")
    private Integer duration;
    
    @Schema(description = "封面URL")
    private String coverUrl;
    
    @Schema(description = "课件描述")
    private String description;
    
    @Schema(description = "排序号")
    private Integer sortOrder;
    
    @Schema(description = "是否允许下载: 0-否 1-是")
    private Integer allowDownload = 0;
}

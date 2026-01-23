package com.edu.platform.resource.dto.response;

import lombok.Data;

/**
 * 附件上传响应DTO
 *
 * @author Education Platform
 */
@Data
public class AttachmentUploadResponse {
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 文件大小
     */
    private Long fileSize;
    
    /**
     * 文件类型: video/pdf/image/doc
     */
    private String fileType;
    
    /**
     * MIME类型
     */
    private String mimeType;
    
    /**
     * 视频时长(秒)
     */
    private Integer duration;
    
    /**
     * 视频宽度
     */
    private Integer videoWidth;
    
    /**
     * 视频高度
     */
    private Integer videoHeight;
    
    /**
     * 视频缩略图URL
     */
    private String thumbnailUrl;
    
    /**
     * PDF页数
     */
    private Integer pageCount;
    
}

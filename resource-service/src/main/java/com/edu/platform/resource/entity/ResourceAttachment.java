package com.edu.platform.resource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源附件表
 *
 * @author Education Platform
 */
@Data
@TableName("resource_attachment")
public class ResourceAttachment {
    
    /**
     * 附件ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 资源ID
     */
    private Long resourceId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件URL(OSS)
     */
    private String fileUrl;
    
    /**
     * 文件大小(字节)
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
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    /**
     * 逻辑删除标志 (0:未删除 1:已删除)
     */
    @TableLogic
    private Integer isDeleted;
    
}

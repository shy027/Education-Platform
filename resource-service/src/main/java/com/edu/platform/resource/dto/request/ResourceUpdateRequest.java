package com.edu.platform.resource.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 更新资源请求DTO
 *
 * @author Education Platform
 */
@Data
public class ResourceUpdateRequest {
    
    /**
     * 资源标题
     */
    private String title;
    
    /**
     * 富文本内容(HTML格式)
     */
    private String content;
    
    /**
     * 资源摘要
     */
    private String summary;
    
    /**
     * 封面图片URL
     */
    private String coverUrl;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 资源类型: 1-文章, 2-视频, 3-文档, 4-音频
     */
    private Integer resourceType;

    /**
     * 便捷上传文件URL
     */
    private String fileUrl;
    

    
    /**
     * 标签ID列表
     */
    @jakarta.validation.constraints.NotEmpty(message = "资源标签不能为空")
    private List<Long> tagIds;
    
    /**
     * 附件列表
     */
    private List<ResourceAttachmentRequest> attachments;
    
}

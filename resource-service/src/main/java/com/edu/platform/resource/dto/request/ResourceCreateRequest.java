package com.edu.platform.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建资源请求DTO
 *
 * @author Education Platform
 */
@Data
public class ResourceCreateRequest {
    
    /**
     * 资源标题
     */
    @NotBlank(message = "资源标题不能为空")
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
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    

    
    /**
     * 标签ID列表
     */
    private List<Long> tagIds;
    
    /**
     * 附件列表
     */
    private List<ResourceAttachmentRequest> attachments;
    
}

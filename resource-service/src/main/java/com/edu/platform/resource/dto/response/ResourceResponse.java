package com.edu.platform.resource.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源响应DTO(列表)
 *
 * @author Education Platform
 */
@Data
public class ResourceResponse {
    
    /**
     * 资源ID
     */
    private Long id;
    
    /**
     * 资源标题
     */
    private String title;
    
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
     * 分类名称
     */
    private String categoryName;
    

    
    /**
     * 状态: 0-草稿, 1-待审核, 2-已发布, 3-已拒绝, 4-已下架
     */
    private Integer status;
    
    /**
     * 审核状态: 0-待审核, 1-通过, 2-拒绝
     */
    private Integer auditStatus;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 创建者名称
     */
    private String creatorName;
    
    /**
     * 创建者类型: 1-管理员, 2-教师
     */
    private Integer creatorType;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 点赞次数
     */
    private Integer likeCount;
    
    /**
     * 收藏次数
     */
    private Integer collectCount;
    
    /**
     * 标签列表
     */
    private List<TagInfo> tags;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishedTime;
    
    /**
     * 标签信息
     */
    @Data
    public static class TagInfo {
        private Long id;
        private String tagName;
        private String tagColor;
    }
    
}

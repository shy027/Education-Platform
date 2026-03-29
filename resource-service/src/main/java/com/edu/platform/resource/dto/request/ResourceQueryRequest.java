package com.edu.platform.resource.dto.request;

import lombok.Data;

/**
 * 资源查询请求DTO
 *
 * @author Education Platform
 */
@Data
public class ResourceQueryRequest {
    
    /**
     * 标题关键词
     */
    private String keyword;
    
    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 资源类型: 1-文章, 2-视频, 3-文档, 4-音频
     */
    private Integer resourceType;
    
    /**
     * 状态列表: 0-草稿, 1-待审核, 2-已发布, 3-已拒绝, 4-已下架
     */
    private java.util.List<Integer> statusList;

    /**
     * 单个状态 (兼容旧逻辑)
     */
    private Integer status;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    

    
    /**
     * 标签ID
     */
    private Long tagId;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 排序方式 (newest/views)
     */
    private String sortMode;
    
}

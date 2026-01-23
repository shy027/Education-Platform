package com.edu.platform.resource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源主表
 *
 * @author Education Platform
 */
@Data
@TableName("resource")
public class Resource {
    
    /**
     * 资源ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
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
     * 状态: 0-草稿, 1-待审核, 2-已发布, 3-已拒绝, 4-已下架
     */
    private Integer status;
    
    /**
     * 审核状态: 0-待审核, 1-通过, 2-拒绝
     */
    private Integer auditStatus;
    
    /**
     * 审核备注
     */
    private String auditRemark;
    
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    
    /**
     * 审核人ID
     */
    private Long auditorId;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
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
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishedTime;
    
    /**
     * 逻辑删除标志 (0:未删除 1:已删除)
     */
    @TableLogic
    private Integer isDeleted;
    
}

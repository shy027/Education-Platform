package com.edu.platform.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 讨论帖子表
 *
 * @author Education Platform
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_post")
public class CommunityPost extends BaseEntity {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 发帖人ID
     */
    private Long userId;
    
    /**
     * 帖子标题
     */
    private String postTitle;
    
    /**
     * 帖子内容
     */
    private String postContent;
    
    /**
     * 附件URLs(JSON)
     */
    private String attachmentUrls;
    
    /**
     * 是否置顶 (0:否 1:是)
     */
    private Integer isTop;
    
    /**
     * 是否精华 (0:否 1:是)
     */
    private Integer isEssence;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 审核状态 (0:待审核 1:通过 2:拒绝)
     */
    private Integer auditStatus;
    
    /**
     * 状态 (0:删除 1:正常 2:锁定)
     */
    private Integer status;
    
}

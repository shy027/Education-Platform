package com.edu.platform.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论表
 *
 * @author Education Platform
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_comment")
public class CommunityComment extends BaseEntity {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 评论人ID
     */
    private Long userId;
    
    /**
     * 父评论ID (0为一级)
     */
    private Long parentId;
    
    /**
     * 回复给谁
     */
    private Long replyToUserId;
    
    /**
     * 评论内容
     */
    private String commentContent;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 审核状态 (0:待审核 1:通过 2:拒绝)
     */
    private Integer auditStatus;
    
    /**
     * 状态 (0:删除 1:正常)
     */
    private Integer status;
    
}

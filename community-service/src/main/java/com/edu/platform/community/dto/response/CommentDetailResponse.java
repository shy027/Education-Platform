package com.edu.platform.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 观点详情响应
 *
 * @author Education Platform
 */
@Data
@Schema(description = "观点详情响应")
public class CommentDetailResponse {
    
    @Schema(description = "评论ID")
    private Long id;
    
    @Schema(description = "话题ID")
    private Long postId;
    
    @Schema(description = "发表者ID")
    private Long userId;
    
    @Schema(description = "发表者姓名")
    private String userName;
    
    @Schema(description = "发表者头像")
    private String userAvatar;
    
    @Schema(description = "观点内容")
    private String commentContent;
    
    @Schema(description = "父评论ID")
    private Long parentId;
    
    @Schema(description = "回复的用户ID")
    private Long replyToUserId;
    
    @Schema(description = "回复的用户名")
    private String replyToUserName;
    
    @Schema(description = "点赞数")
    private Integer likeCount;
    
    @Schema(description = "状态")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
    
    @Schema(description = "子回复列表(二级评论)")
    private List<CommentDetailResponse> replies;
    
}

package com.edu.platform.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子详情响应
 *
 * @author Education Platform
 */
@Data
@Schema(description = "帖子详情响应")
public class PostDetailResponse {
    
    @Schema(description = "帖子ID")
    private Long id;
    
    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "发帖人ID")
    private Long userId;
    
    @Schema(description = "发帖人姓名")
    private String userName;
    
    @Schema(description = "发帖人头像")
    private String userAvatar;
    
    @Schema(description = "帖子标题")
    private String postTitle;
    
    @Schema(description = "帖子内容")
    private String postContent;
    
    @Schema(description = "附件URLs")
    private String attachmentUrls;
    
    @Schema(description = "是否置顶")
    private Integer isTop;
    
    @Schema(description = "是否精华")
    private Integer isEssence;
    
    @Schema(description = "浏览次数")
    private Integer viewCount;
    
    @Schema(description = "点赞数")
    private Integer likeCount;
    
    @Schema(description = "评论数")
    private Integer commentCount;
    
    @Schema(description = "状态")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
    
}

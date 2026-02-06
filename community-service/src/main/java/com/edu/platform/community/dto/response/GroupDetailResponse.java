package com.edu.platform.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小组详情响应
 *
 * @author Education Platform
 */
@Data
@Schema(description = "小组详情响应")
public class GroupDetailResponse {
    
    @Schema(description = "小组ID")
    private Long groupId;
    
    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "小组名称")
    private String groupName;
    
    @Schema(description = "小组简介")
    private String groupIntro;
    
    @Schema(description = "创建人ID")
    private Long creatorId;
    
    @Schema(description = "创建人姓名")
    private String creatorName;
    
    @Schema(description = "创建人头像")
    private String creatorAvatar;
    
    @Schema(description = "最大成员数")
    private Integer maxMembers;
    
    @Schema(description = "当前成员数")
    private Integer memberCount;
    
    @Schema(description = "状态:0解散,1正常")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
    
    @Schema(description = "当前用户是否已加入")
    private Boolean isJoined;
    
    @Schema(description = "当前用户加入状态:0待审批,1已同意,2已拒绝")
    private Integer joinStatus;
}

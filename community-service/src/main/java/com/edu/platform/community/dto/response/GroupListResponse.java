package com.edu.platform.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 小组列表响应
 *
 * @author Education Platform
 */
@Data
@Schema(description = "小组列表响应")
public class GroupListResponse {
    
    @Schema(description = "小组ID")
    private Long groupId;
    
    @Schema(description = "小组名称")
    private String groupName;
    
    @Schema(description = "小组简介")
    private String groupIntro;
    
    @Schema(description = "创建人姓名")
    private String creatorName;
    
    @Schema(description = "当前成员数")
    private Integer memberCount;
    
    @Schema(description = "最大成员数")
    private Integer maxMembers;
    
    @Schema(description = "状态:0解散,1正常")
    private Integer status;
}

package com.edu.platform.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小组成员响应
 *
 * @author Education Platform
 */
@Data
@Schema(description = "小组成员响应")
public class GroupMemberResponse {
    
    @Schema(description = "成员ID")
    private Long memberId;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户姓名")
    private String userName;
    
    @Schema(description = "用户头像")
    private String userAvatar;
    
    @Schema(description = "成员角色:1组长,2成员")
    private Integer memberRole;
    
    @Schema(description = "加入状态:0待审批,1已同意,2已拒绝")
    private Integer joinStatus;
    
    @Schema(description = "加入时间")
    private LocalDateTime joinTime;
}

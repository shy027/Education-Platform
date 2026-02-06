package com.edu.platform.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小组成员实体类
 *
 * @author Education Platform
 */
@Data
@TableName("community_group_member")
public class CommunityGroupMember {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 小组ID
     */
    private Long groupId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色:1组长,2成员
     */
    private Integer memberRole;
    
    /**
     * 加入状态:0待审批,1已同意,2已拒绝
     */
    private Integer joinStatus;
    
    /**
     * 审批时间
     */
    private LocalDateTime approveTime;
    
    /**
     * 审批人ID(教师)
     */
    private Long approverId;
    
    /**
     * 加入时间
     */
    private LocalDateTime joinTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}

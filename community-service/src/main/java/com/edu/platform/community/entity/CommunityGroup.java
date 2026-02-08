package com.edu.platform.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小组实体类
 *
 * @author Education Platform
 */
@Data
@TableName("community_group")
public class CommunityGroup {
    
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
     * 小组名称
     */
    private String groupName;
    
    /**
     * 环信群组ID
     */
    private String easemobGroupId;
    
    /**
     * 小组简介
     */
    private String groupIntro;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 最大成员数
     */
    private Integer maxMembers;
    
    /**
     * 当前成员数
     */
    private Integer memberCount;
    
    /**
     * 状态:0解散,1正常
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
    
    /**
     * 逻辑删除:0否,1是
     */
    @TableLogic
    private Integer isDeleted;
}

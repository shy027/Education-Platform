package com.edu.platform.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小组讨论话题实体类
 */
@Data
@TableName("group_topic")
public class GroupTopic {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 小组ID
     */
    private Long groupId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 创建者ID(教师)
     */
    private Long creatorId;
    
    /**
     * 话题标题
     */
    private String title;
    
    /**
     * 话题内容/要求
     */
    private String content;
    
    /**
     * 截止时间
     */
    private LocalDateTime deadline;
    
    /**
     * 状态:1进行中,2已结束
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
     * 是否删除:0否,1是
     */
    @TableLogic
    private Integer isDeleted;
}

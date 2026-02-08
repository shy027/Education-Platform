package com.edu.platform.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小组协作文档实体类
 */
@Data
@TableName("group_document")
public class GroupDocument {
    
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
     * 关联话题ID
     */
    private Long topicId;
    
    /**
     * 文档标题
     */
    private String title;
    
    /**
     * 文档内容(富文本)
     */
    private String content;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 最后编辑者ID
     */
    private Long lastEditorId;
    
    /**
     * 最后编辑时间
     */
    private LocalDateTime lastEditTime;
    
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

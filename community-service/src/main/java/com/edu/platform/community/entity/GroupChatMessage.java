package com.edu.platform.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小组聊天消息实体类
 */
@Data
@TableName("group_chat_message")
public class GroupChatMessage {
    
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
     * 关联话题ID(可选)
     */
    private Long topicId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 消息类型:1文本,2图片,3文件
     */
    private Integer messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 文件名称
     */
    private String fileName;
    
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * 发送时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 是否删除:0否,1是
     */
    @TableLogic
    private Integer isDeleted;
}

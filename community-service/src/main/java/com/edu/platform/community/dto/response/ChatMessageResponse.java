package com.edu.platform.community.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息响应
 */
@Data
@Schema(description = "聊天消息响应")
public class ChatMessageResponse {
    
    @Schema(description = "消息ID")
    private Long messageId;
    
    @Schema(description = "小组ID")
    private Long groupId;
    
    @Schema(description = "关联话题ID")
    private Long topicId;
    
    @Schema(description = "发送者ID")
    private Long senderId;
    
    @Schema(description = "发送者姓名")
    private String senderName;
    
    @Schema(description = "发送者头像")
    private String senderAvatar;
    
    @Schema(description = "消息类型:1文本,2图片,3文件")
    private Integer messageType;
    
    @Schema(description = "消息内容")
    private String content;
    
    @Schema(description = "文件URL")
    private String fileUrl;
    
    @Schema(description = "文件名称")
    private String fileName;
    
    @Schema(description = "文件大小(字节)")
    private Long fileSize;
    
    @Schema(description = "发送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdTime;
}

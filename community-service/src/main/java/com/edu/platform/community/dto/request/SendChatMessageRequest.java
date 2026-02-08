package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送聊天消息请求
 */
@Data
@Schema(description = "发送聊天消息请求")
public class SendChatMessageRequest {
    
    @Schema(description = "消息类型:1文本,2图片,3文件")
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;
    
    @Schema(description = "消息内容(文本消息必填)")
    private String content;
    
    @Schema(description = "文件URL(图片/文件消息必填)")
    private String fileUrl;
    
    @Schema(description = "文件名称")
    private String fileName;
    
    @Schema(description = "文件大小(字节)")
    private Long fileSize;
    
    @Schema(description = "关联话题ID(可选)")
    private Long topicId;
}

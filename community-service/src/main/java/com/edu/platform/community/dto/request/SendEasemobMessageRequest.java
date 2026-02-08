package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 环信消息发送请求
 */
@Data
@Schema(description = "环信消息发送请求")
public class SendEasemobMessageRequest {
    
    @NotNull(message = "小组ID不能为空")
    @Schema(description = "小组ID", example = "1")
    private Long groupId;
    
    @Schema(description = "话题ID(可选)", example = "1")
    private Long topicId;
    
    @NotNull(message = "消息类型不能为空")
    @Schema(description = "消息类型: 1-文本, 2-图片, 3-文件", example = "1")
    private Integer messageType;
    
    @Schema(description = "消息内容(文本消息必填)", example = "Hello, everyone!")
    private String content;
    
    @Schema(description = "文件URL(图片/文件消息必填)")
    private String fileUrl;
    
    @Schema(description = "文件名称")
    private String fileName;
    
    @Schema(description = "文件大小(字节)")
    private Long fileSize;
}

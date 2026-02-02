package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表观点请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "发表观点请求")
public class CreateCommentRequest {
    
    @Schema(description = "话题ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "话题ID不能为空")
    private Long postId;
    
    @Schema(description = "观点内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "观点内容不能为空")
    @Size(min = 1, max = 1000, message = "观点内容长度为1-1000字符")
    private String commentContent;
    
    @Schema(description = "父评论ID(0表示一级观点,非0表示回复某个观点)", example = "0")
    @NotNull(message = "父评论ID不能为空")
    private Long parentId = 0L;
    
    @Schema(description = "回复的用户ID(二级回复时必填)")
    private Long replyToUserId;
    
}

package com.edu.platform.audit.dto.feign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评论信息DTO
 *
 * @author Education Platform
 */
@Data
@Schema(description = "评论信息")
public class CommentInfoDTO {
    
    @Schema(description = "评论ID")
    private Long id;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "作者ID")
    private Long authorId;
    
    @Schema(description = "作者姓名")
    private String authorName;
    
}

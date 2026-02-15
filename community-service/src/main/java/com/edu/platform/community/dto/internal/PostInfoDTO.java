package com.edu.platform.community.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 帖子信息DTO(内部调用)
 *
 * @author Education Platform
 */
@Data
@Schema(description = "帖子信息")
public class PostInfoDTO {
    
    @Schema(description = "帖子ID")
    private Long id;
    
    @Schema(description = "帖子标题")
    private String title;
    
    @Schema(description = "帖子内容")
    private String content;
    
    @Schema(description = "作者ID")
    private Long authorId;
    
    @Schema(description = "作者姓名")
    private String authorName;
    
}

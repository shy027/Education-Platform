package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 观点查询请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "观点查询请求")
public class CommentQueryRequest {
    
    @Schema(description = "话题ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long postId;
    
    @Schema(description = "父评论id(查询子评论时使用)")
    private Long parentId;
    
    @Schema(description = "排序方式 (latest:最新 hot:最热)", example = "latest")
    private String orderBy = "latest";
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量", example = "20")
    private Integer pageSize = 20;
    
}

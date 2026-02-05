package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 帖子查询请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "帖子查询请求")
public class PostQueryRequest {
    
    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long courseId;
    
    @Schema(description = "用户ID(用于查询我的帖子)")
    private Long userId;
    
    @Schema(description = "关键词搜索(标题或内容)")
    private String keyword;
    
    @Schema(description = "是否置顶 (0:否 1:是)")
    private Integer isTop;
    
    @Schema(description = "是否精华 (0:否 1:是)")
    private Integer isEssence;
    
    @Schema(description = "排序方式 (latest:最新 hot:最热)", example = "latest")
    private String orderBy = "latest";
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
    
}

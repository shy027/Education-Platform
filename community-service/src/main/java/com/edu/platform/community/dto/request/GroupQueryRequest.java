package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 小组查询请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "小组查询请求")
public class GroupQueryRequest {
    
    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "状态:0解散,1正常")
    private Integer status;
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}

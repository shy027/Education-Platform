package com.edu.platform.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞响应
 *
 * @author Education Platform
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "点赞响应")
public class LikeResponse {
    
    @Schema(description = "是否已点赞")
    private Boolean liked;
    
    @Schema(description = "点赞总数")
    private Integer likeCount;
}

package com.edu.platform.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 课件更新请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "课件更新请求")
public class CoursewareUpdateRequest {
    
    @NotNull(message = "课件ID不能为空")
    @Schema(description = "课件ID")
    private Long id;
    
    @Schema(description = "课件标题")
    private String wareTitle;
    
    @Schema(description = "封面URL")
    private String coverUrl;
    
    @Schema(description = "课件描述")
    private String description;
    
    @Schema(description = "排序号")
    private Integer sortOrder;
    
    @Schema(description = "是否允许下载: 0-否 1-是")
    private Integer allowDownload;
}

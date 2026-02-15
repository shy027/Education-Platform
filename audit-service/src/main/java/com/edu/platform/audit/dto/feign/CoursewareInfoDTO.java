package com.edu.platform.audit.dto.feign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课件信息DTO
 *
 * @author Education Platform
 */
@Data
@Schema(description = "课件信息")
public class CoursewareInfoDTO {
    
    @Schema(description = "课件ID")
    private Long id;
    
    @Schema(description = "课件标题")
    private String title;
    
    @Schema(description = "课件描述")
    private String description;
    
    @Schema(description = "创建者ID")
    private Long creatorId;
    
    @Schema(description = "创建者姓名")
    private String creatorName;
    
}

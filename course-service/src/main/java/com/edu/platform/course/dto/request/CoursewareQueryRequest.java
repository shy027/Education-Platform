package com.edu.platform.course.dto.request;

import com.edu.platform.common.dto.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课件查询请求
 *
 * @author Education Platform
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课件查询请求")
public class CoursewareQueryRequest extends PageRequest {
    
    @Schema(description = "章节ID")
    private Long chapterId;
    
    @Schema(description = "课件类型: 1-视频 2-文档 3-音频")
    private Integer wareType;
    
    @Schema(description = "审核状态: 0-待审核 1-已通过 2-未通过")
    private Integer auditStatus;
}

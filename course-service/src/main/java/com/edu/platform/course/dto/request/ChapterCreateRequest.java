package com.edu.platform.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建章节请求
 */
@Data
public class ChapterCreateRequest {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    /**
     * 父章节ID (0表示一级章节)
     */
    private Long parentId = 0L;
    
    @NotBlank(message = "章节名称不能为空")
    private String chapterName;
    
    private String chapterIntro;
    
    private Integer sortOrder = 0;
}

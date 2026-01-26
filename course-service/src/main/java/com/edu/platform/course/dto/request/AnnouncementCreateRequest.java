package com.edu.platform.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建公告请求
 */
@Data
public class AnnouncementCreateRequest {

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空")
    private String title;
    
    /**
     * 公告内容
     */
    @NotBlank(message = "公告内容不能为空")
    private String content;
    
    /**
     * 是否置顶 (0否 1是)
     */
    private Integer isTop = 0;
}

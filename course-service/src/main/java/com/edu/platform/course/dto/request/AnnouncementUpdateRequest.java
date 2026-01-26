package com.edu.platform.course.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新公告请求
 */
@Data
public class AnnouncementUpdateRequest {

    /**
     * 公告ID
     */
    @NotNull(message = "公告ID不能为空")
    private Long id;
    
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 公告内容
     */
    private String content;
    
    /**
     * 是否置顶
     */
    private Integer isTop;
}

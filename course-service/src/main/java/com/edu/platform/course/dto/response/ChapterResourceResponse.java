package com.edu.platform.course.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 章节关联资源响应
 */
@Data
public class ChapterResourceResponse {
    
    private Long id; // course_chapter_resource 表的 id (绑定关系 id)
    
    private Long resourceId;
    
    private String title;
    
    private Integer resourceType; // 1文章, 2视频, 3文档, 4音频
    
    private String coverUrl;
    
    private String fileUrl;
    
    private LocalDateTime bindTime; // 绑定时间
}

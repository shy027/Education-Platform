package com.edu.platform.course.dto.request;

import lombok.Data;

/**
 * 更新章节请求
 */
@Data
public class ChapterUpdateRequest {

    private Long id;
    
    private String chapterName;
    
    private String chapterIntro;
    
    private Integer sortOrder;
}

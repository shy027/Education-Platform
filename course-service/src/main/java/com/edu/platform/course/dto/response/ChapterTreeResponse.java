package com.edu.platform.course.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 章节树形响应
 */
@Data
public class ChapterTreeResponse {

    private Long id;
    
    private Long courseId;
    
    private Long parentId;
    
    private String chapterName;
    
    private String chapterIntro;
    
    private Integer sortOrder;
    
    /**
     * 子章节
     */
    private List<ChapterTreeResponse> children;
}

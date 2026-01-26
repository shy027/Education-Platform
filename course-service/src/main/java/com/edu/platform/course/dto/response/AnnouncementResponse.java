package com.edu.platform.course.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告响应
 */
@Data
public class AnnouncementResponse {

    /**
     * 公告ID
     */
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
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
    
    /**
     * 发布者ID
     */
    private Long publisherId;
    
    /**
     * 发布者姓名
     */
    private String publisherName;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}

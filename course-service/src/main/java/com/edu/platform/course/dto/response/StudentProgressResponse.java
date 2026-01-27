package com.edu.platform.course.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生学习进度响应
 *
 * @author Education Platform
 */
@Data
public class StudentProgressResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 学号
     */
    private String studentNumber;
    
    /**
     * 课件ID
     */
    private Long wareId;
    
    /**
     * 课件标题
     */
    private String wareTitle;
    
    /**
     * 学习进度(秒)
     */
    private Integer progressSeconds;
    
    /**
     * 课件时长(秒)
     */
    private Integer duration;
    
    /**
     * 进度百分比
     */
    private Integer progressPercent;
    
    /**
     * 是否完成: 0-未完成 1-已完成
     */
    private Integer completed;
    
    /**
     * 最后观看时间
     */
    private LocalDateTime lastViewTime;
}

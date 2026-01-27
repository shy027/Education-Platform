package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课件学习进度实体
 *
 * @author Education Platform
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("courseware_progress")
public class CoursewareProgress {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 课件ID
     */
    private Long wareId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 学习进度(秒)
     */
    private Integer progressSeconds;
    
    /**
     * 是否完成: 0-未完成 1-已完成
     */
    private Integer completed;
    
    /**
     * 最后观看时间
     */
    private LocalDateTime lastViewTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}

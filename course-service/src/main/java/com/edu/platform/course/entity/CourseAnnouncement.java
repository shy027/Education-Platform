package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课程公告表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_announcement")
public class CourseAnnouncement extends BaseEntity {

    @com.baomidou.mybatisplus.annotation.TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;


    private Long courseId;
    
    private String title;
    
    private String content;
    
    private Integer isTop;
    
    private Long publisherId;
    
    private LocalDateTime publishTime;
    
    private Integer viewCount;
    
    /**
     * 状态:0撤回,1发布
     */
    private Integer status;
}

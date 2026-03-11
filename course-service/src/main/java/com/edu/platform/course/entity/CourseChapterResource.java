package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程章节与资源库关联实体
 */
@Data
@TableName("course_chapter_resource")
public class CourseChapterResource {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 章节ID
     */
    private Long chapterId;

    /**
     * 资源库ID
     */
    private Long resourceId;

    /**
     * 绑定人ID
     */
    private Long creatorId;

    /**
     * 绑定时间
     */
    private LocalDateTime createdTime;
}

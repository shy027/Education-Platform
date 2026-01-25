package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 课程章节表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_chapter")
public class CourseChapter extends BaseEntity {

    @com.baomidou.mybatisplus.annotation.TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;


    private Long courseId;
    
    private Long parentId;
    
    private String chapterName;
    
    private String chapterIntro;
    
    private Integer sortOrder;
    
    /**
     * 状态:0隐藏,1发布
     */
    private Integer status;
    
    @TableField(exist = false)
    private List<CourseChapter> children;
}

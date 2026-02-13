package com.edu.platform.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 课程信息表(简化版,仅用于查询教师ID和课程名称)
 *
 * @author Education Platform
 */
@Data
@TableName("course_info")
public class CourseInfo {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程编码
     */
    private String courseCode;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 主讲教师ID
     */
    private Long teacherId;
}

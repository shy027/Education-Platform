package com.edu.platform.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程报告实体
 *
 * @author Education Platform
 */
@Data
@TableName("report_course_report")
public class CourseReport implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
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
     * 报告类型:1课程报告,2学校报告
     */
    private Integer reportType;
    
    /**
     * 报告标题
     */
    private String reportTitle;
    
    /**
     * 报告数据(JSON)
     */
    private String reportData;
    
    /**
     * PDF文件URL
     */
    private String fileUrl;
    
    /**
     * 生成人ID
     */
    private Long generatorId;
    
    /**
     * 生成时间
     */
    private LocalDateTime generateTime;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
}

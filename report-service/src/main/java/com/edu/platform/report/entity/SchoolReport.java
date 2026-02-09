package com.edu.platform.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学校报告实体
 *
 * @author Education Platform
 */
@Data
@TableName("report_school_report")
public class SchoolReport implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学校ID
     */
    private Long schoolId;
    
    /**
     * 报告周期(如2026-Q1)
     */
    private String reportPeriod;
    
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

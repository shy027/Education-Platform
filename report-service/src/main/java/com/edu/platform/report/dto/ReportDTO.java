package com.edu.platform.report.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报告DTO
 *
 * @author Education Platform
 */
@Data
public class ReportDTO {
    
    /**
     * 报告ID
     */
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 报告标题
     */
    private String reportTitle;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 报告类型(1=课程报告,2=学校报告)
     */
    private Integer reportType;
    
    /**
     * 生成人ID
     */
    private Long generatorId;
    
    /**
     * 生成人姓名
     */
    private String generatorName;
    
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

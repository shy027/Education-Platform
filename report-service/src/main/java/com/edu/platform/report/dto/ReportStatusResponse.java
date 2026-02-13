package com.edu.platform.report.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报告状态响应
 *
 * @author Education Platform
 */
@Data
public class ReportStatusResponse {
    
    /**
     * 报告ID
     */
    private Long reportId;
    
    /**
     * 状态(completed=完成, failed=失败)
     */
    private String status;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 生成时间
     */
    private LocalDateTime generateTime;
    
    /**
     * 错误信息(失败时)
     */
    private String errorMessage;
}

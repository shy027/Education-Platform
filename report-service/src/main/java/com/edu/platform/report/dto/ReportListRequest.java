package com.edu.platform.report.dto;

import lombok.Data;

/**
 * 报告列表查询请求
 *
 * @author Education Platform
 */
@Data
public class ReportListRequest {
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 报告类型(1=课程报告,2=学校报告)
     */
    private Integer reportType;
    
    /**
     * 开始时间
     */
    private String startTime;
    
    /**
     * 结束时间
     */
    private String endTime;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}

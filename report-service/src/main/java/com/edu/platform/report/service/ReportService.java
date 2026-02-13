package com.edu.platform.report.service;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.report.dto.ReportDTO;
import com.edu.platform.report.dto.ReportListRequest;
import com.edu.platform.report.dto.ReportStatusResponse;

/**
 * 报告服务接口
 *
 * @author Education Platform
 */
public interface ReportService {
    
    /**
     * 生成课程报告
     *
     * @param courseId 课程ID
     * @param userId 生成人ID
     * @return 报告ID
     */
    Long generateCourseReport(Long courseId, Long userId);
    
    /**
     * 生成学校报告
     *
     * @param schoolId 学校ID
     * @return 报告ID
     */
    Long generateSchoolReport(Long schoolId);
    
    /**
     * 获取报告文件路径
     *
     * @param reportId 报告ID
     * @return 文件路径
     */
    String getReportFilePath(Long reportId);
    
    /**
     * 查询报告状态
     *
     * @param reportId 报告ID
     * @return 报告状态
     */
    ReportStatusResponse getReportStatus(Long reportId);
    
    /**
     * 生成下载URL
     *
     * @param reportId 报告ID
     * @return 预签名下载URL
     */
    String generateDownloadUrl(Long reportId);
    
    /**
     * 增加下载次数
     *
     * @param reportId 报告ID
     */
    void incrementDownloadCount(Long reportId);
    
    /**
     * 查询课程报告列表
     *
     * @param courseId 课程ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<ReportDTO> getCourseReports(Long courseId, Integer pageNum, Integer pageSize);
    
    /**
     * 查询所有报告列表(管理员)
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<ReportDTO> getReportList(ReportListRequest request);
    
    /**
     * 删除报告
     *
     * @param reportId 报告ID
     */
    void deleteReport(Long reportId);
}

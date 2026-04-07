package com.edu.platform.report.controller;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.report.dto.ReportDTO;
import com.edu.platform.report.dto.ReportListRequest;
import com.edu.platform.report.dto.ReportStatusResponse;
import com.edu.platform.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 报告控制器
 *
 * @author Education Platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "报告管理", description = "报告生成和下载接口")
public class ReportController {
    
    private final ReportService reportService;
    
    /**
     * 生成课程报告
     */
    @PostMapping("/course/{courseId}/generate")
    @Operation(summary = "生成课程报告", description = "生成指定课程的思政教学成效报告")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_SCHOOL_LEADER', 'ROLE_ADMIN')")
    public Result<Long> generateCourseReport(
            @Parameter(description = "课程ID", required = true)
            @PathVariable Long courseId) {
        
        try {
            Long userId = com.edu.platform.common.utils.UserContext.getUserId();
            log.info("接收到生成课程报告请求: courseId={}, userId={}", courseId, userId);
            
            if (userId == null) {
                log.error("生成报告失败: 无法获取当前登录用户ID (UserContext 为空)");
                return Result.fail("无法获取用户信息，请重新登录");
            }
            
            Long reportId = reportService.generateCourseReport(courseId, userId);
            log.info("课程报告生成成功: reportId={}, courseId={}, userId={}", reportId, courseId, userId);
            return Result.success(reportId);
        } catch (Exception e) {
            log.error("生成课程报告失败", e);
            return Result.fail("生成报告失败: " + e.getMessage());
        }
    }

    /**
     * 生成学校报告
     */
    @PostMapping("/school/{schoolId}/generate")
    @Operation(summary = "生成学校报告", description = "按照指定时间范围生成学校全域思政建设成效报告")
    @PreAuthorize("hasAnyAuthority('ROLE_SCHOOL_LEADER', 'ROLE_ADMIN')")
    public Result<Long> generateSchoolReport(
            @Parameter(description = "学校ID", required = true)
            @PathVariable Long schoolId,
            @Parameter(description = "开始时间(yyyy-MM-dd)", required = false)
            @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间(yyyy-MM-dd)", required = false)
            @RequestParam(required = false) String endTime) {
        
        try {
            Long reportId = reportService.generateSchoolReport(schoolId, startTime, endTime);
            return Result.success(reportId);
        } catch (Exception e) {
            log.error("生成学校报告失败", e);
            return Result.fail("生成报告失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询报告状态
     */
    @GetMapping("/{reportId}/status")
    @Operation(summary = "查询报告状态", description = "查询指定报告的生成状态")
    public Result<ReportStatusResponse> getReportStatus(
            @Parameter(description = "报告ID", required = true)
            @PathVariable Long reportId) {
        
        try {
            ReportStatusResponse status = reportService.getReportStatus(reportId);
            return Result.success(status);
        } catch (Exception e) {
            log.error("查询报告状态失败", e);
            return Result.fail("查询报告状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载报告(获取预签名URL)
     */
    @GetMapping("/{reportId}/download")
    @Operation(summary = "下载报告", description = "获取报告的预签名下载URL(有效期1小时)")
    public Result<String> downloadReport(
            @Parameter(description = "报告ID", required = true)
            @PathVariable Long reportId,
            @Parameter(description = "报告类型(1=课程,2=学校)", required = false)
            @RequestParam(required = false) Integer reportType) {
        
        try {
            String downloadUrl = reportService.generateDownloadUrl(reportId, reportType);
            // 增加下载次数
            reportService.incrementDownloadCount(reportId, reportType);
            return Result.success(downloadUrl);
        } catch (Exception e) {
            log.error("获取下载链接失败", e);
            return Result.fail("获取下载链接失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询课程报告列表
     */
    @GetMapping("/course/{courseId}")
    @Operation(summary = "查询课程报告列表", description = "分页查询指定课程的报告列表")
    public Result<PageResult<ReportDTO>> getCourseReports(
            @Parameter(description = "课程ID", required = true)
            @PathVariable Long courseId,
            @Parameter(description = "页码", required = false)
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", required = false)
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        try {
            PageResult<ReportDTO> result = reportService.getCourseReports(courseId, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询课程报告列表失败", e);
            return Result.fail("查询报告列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询所有报告列表(管理员)
     */
    @GetMapping
    @Operation(summary = "查询所有报告列表", description = "管理员/校领导查询所有报告(支持多条件筛选)")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SCHOOL_LEADER')")
    public Result<PageResult<ReportDTO>> getReportList(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer reportType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        try {
            // 构建请求对象
            ReportListRequest request = new ReportListRequest();
            request.setCourseId(courseId);
            request.setReportType(reportType);
            request.setStartTime(startTime);
            request.setEndTime(endTime);
            request.setSchoolId(schoolId);
            request.setPageNum(pageNum);
            request.setPageSize(pageSize);
            
            PageResult<ReportDTO> result = reportService.getReportList(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询报告列表失败", e);
            return Result.fail("查询报告列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除报告
     */
    @DeleteMapping("/{reportId}")
    @Operation(summary = "删除报告", description = "删除指定报告(同时删除OSS文件)")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public Result<Void> deleteReport(
            @Parameter(description = "报告ID", required = true)
            @PathVariable Long reportId) {
        
        try {
            reportService.deleteReport(reportId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除报告失败", e);
            return Result.fail("删除报告失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取报告文件路径(内部使用)
     */
    @GetMapping("/{reportId}/path")
    @Operation(summary = "获取报告文件路径", description = "获取指定报告的OSS文件URL")
    public Result<String> getReportPath(
            @Parameter(description = "报告ID", required = true)
            @PathVariable Long reportId,
            @Parameter(description = "报告类型", required = false)
            @RequestParam(required = false) Integer reportType) {
        
        try {
            String filePath = reportService.getReportFilePath(reportId, reportType);
            return Result.success(filePath);
        } catch (Exception e) {
            log.error("获取报告路径失败", e);
            return Result.fail("获取报告路径失败: " + e.getMessage());
        }
    }
}

package com.edu.platform.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.PageResult;
import com.edu.platform.report.dto.ReportDTO;
import com.edu.platform.report.dto.ReportListRequest;
import com.edu.platform.report.dto.ReportStatusResponse;
import com.edu.platform.report.entity.BehaviorLog;
import com.edu.platform.report.entity.CourseInfo;
import com.edu.platform.report.entity.CourseReport;
import com.edu.platform.report.entity.StudentProfile;
import com.edu.platform.report.entity.UserAccount;
import com.edu.platform.report.generator.PdfGenerator;
import com.edu.platform.report.mapper.BehaviorLogMapper;
import com.edu.platform.report.mapper.CourseInfoMapper;
import com.edu.platform.report.mapper.CourseReportMapper;
import com.edu.platform.report.mapper.StudentProfileMapper;
import com.edu.platform.report.mapper.UserAccountMapper;
import com.edu.platform.report.service.OssFileService;
import com.edu.platform.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报告服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    
    private final PdfGenerator pdfGenerator;
    private final CourseReportMapper courseReportMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final BehaviorLogMapper behaviorLogMapper;
    private final UserAccountMapper userAccountMapper;
    private final CourseInfoMapper courseInfoMapper;
    private final OssFileService ossFileService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateCourseReport(Long courseId, Long userId) {
        try {
            log.info("开始生成课程报告: courseId={}, userId={}", courseId, userId);
            
            // 1. 创建报告记录
            CourseReport report = new CourseReport();
            report.setCourseId(courseId);
            report.setReportType(1); // 1=课程报告
            report.setReportTitle("课程思政教学成效报告");
            report.setGeneratorId(userId); // 使用当前登录用户ID
            report.setGenerateTime(LocalDateTime.now());
            report.setDownloadCount(0);
            courseReportMapper.insert(report);
            
            // 2. 收集数据
            Map<String, Object> data = collectCourseData(courseId);
            
            // 3. 生成PDF
            byte[] pdfBytes = pdfGenerator.generateCourseReport(data);
            
            // 4. 上传到OSS
            String fileName = "course_report_" + courseId + "_" + System.currentTimeMillis() + ".pdf";
            String fileUrl = ossFileService.uploadPdf(pdfBytes, fileName, courseId);
            
            // 5. 更新报告记录
            report.setFileUrl(fileUrl); // 存储OSS URL
            courseReportMapper.updateById(report);
            
            log.info("课程报告生成完成: reportId={}, fileUrl={}, generatorId={}", report.getId(), fileUrl, userId);
            return report.getId();
            
        } catch (Exception e) {
            log.error("生成课程报告失败: courseId={}, userId={}", courseId, userId, e);
            throw new RuntimeException("生成报告失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateSchoolReport(Long schoolId) {
        // 简化实现,暂不支持学校报告
        throw new UnsupportedOperationException("学校报告功能暂未实现");
    }
    
    @Override
    public String getReportFilePath(Long reportId) {
        CourseReport report = courseReportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("报告不存在");
        }
        return report.getFileUrl();
    }
    
    /**
     * 收集课程报告数据
     */
    private Map<String, Object> collectCourseData(Long courseId) {
        Map<String, Object> data = new HashMap<>();
        
        // 1. 查询课程基本信息
        CourseInfo course = courseInfoMapper.selectById(courseId);
        if (course != null) {
            data.put("courseName", course.getCourseName());
            
            // 查询教师姓名
            if (course.getTeacherId() != null) {
                UserAccount teacher = userAccountMapper.selectById(course.getTeacherId());
                if (teacher != null && teacher.getRealName() != null && !teacher.getRealName().isEmpty()) {
                    data.put("teacherName", teacher.getRealName());
                } else {
                    data.put("teacherName", "教师" + course.getTeacherId());
                }
            } else {
                data.put("teacherName", "未知教师");
            }
        } else {
            // 课程不存在时使用默认值
            data.put("courseName", "思政课程-" + courseId);
            data.put("teacherName", "未知教师");
        }
        data.put("reportPeriod", "最近30天");
        
        // 2. 查询学生画像数据
        List<StudentProfile> profiles = studentProfileMapper.selectList(
            new LambdaQueryWrapper<StudentProfile>()
                .eq(StudentProfile::getCourseId, courseId)
        );
        
        data.put("studentCount", profiles.size());
        data.put("activeStudents", profiles.size());
        
        // 3. 统计等级分布
        Map<String, Long> levelCounts = profiles.stream()
            .collect(Collectors.groupingBy(
                p -> p.getLevel() != null ? p.getLevel() : "未评定",
                Collectors.counting()
            ));
        
        data.put("excellentCount", levelCounts.getOrDefault("优秀", 0L));
        data.put("goodCount", levelCounts.getOrDefault("良好", 0L));
        data.put("passCount", levelCounts.getOrDefault("合格", 0L));
        data.put("needImprovementCount", levelCounts.getOrDefault("待提升", 0L));
        
        // 4. 计算平均分
        if (!profiles.isEmpty()) {
            BigDecimal avgScore = profiles.stream()
                .map(StudentProfile::getTotalScore)
                .filter(score -> score != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(profiles.size()), 2, RoundingMode.HALF_UP);
            data.put("avgScore", avgScore);
            
            // 5. 计算五维度平均分
            data.put("dimension1Avg", calculateDimensionAvg(profiles, StudentProfile::getDimension1Score));
            data.put("dimension2Avg", calculateDimensionAvg(profiles, StudentProfile::getDimension2Score));
            data.put("dimension3Avg", calculateDimensionAvg(profiles, StudentProfile::getDimension3Score));
            data.put("dimension4Avg", calculateDimensionAvg(profiles, StudentProfile::getDimension4Score));
            data.put("dimension5Avg", calculateDimensionAvg(profiles, StudentProfile::getDimension5Score));
        } else {
            data.put("avgScore", BigDecimal.ZERO);
            data.put("dimension1Avg", BigDecimal.ZERO);
            data.put("dimension2Avg", BigDecimal.ZERO);
            data.put("dimension3Avg", BigDecimal.ZERO);
            data.put("dimension4Avg", BigDecimal.ZERO);
            data.put("dimension5Avg", BigDecimal.ZERO);
        }
        
        // 6. 统计行为数据
        List<BehaviorLog> behaviorLogs = behaviorLogMapper.selectList(
            new LambdaQueryWrapper<BehaviorLog>()
                .eq(BehaviorLog::getCourseId, courseId)
                .ge(BehaviorLog::getCreatedTime, LocalDateTime.now().minusDays(30))
        );
        
        Map<String, Integer> behaviorStats = behaviorLogs.stream()
            .collect(Collectors.groupingBy(
                BehaviorLog::getBehaviorType,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        
        data.put("behaviorStats", behaviorStats);
        
        // 7. 获取Top学生列表(按总分降序,取前3名)
        List<Map<String, Object>> topStudents = profiles.stream()
            .sorted((p1, p2) -> {
                BigDecimal score1 = p1.getTotalScore() != null ? p1.getTotalScore() : BigDecimal.ZERO;
                BigDecimal score2 = p2.getTotalScore() != null ? p2.getTotalScore() : BigDecimal.ZERO;
                return score2.compareTo(score1); // 降序
            })
            .limit(3)
            .map(profile -> {
                Map<String, Object> student = new HashMap<>();
                student.put("userId", profile.getUserId());
                
                // 查询真实姓名
                UserAccount user = userAccountMapper.selectById(profile.getUserId());
                if (user != null && user.getRealName() != null && !user.getRealName().isEmpty()) {
                    student.put("userName", user.getRealName());
                } else {
                    student.put("userName", "学生" + profile.getUserId());
                }
                
                student.put("totalScore", profile.getTotalScore());
                student.put("level", profile.getLevel());
                return student;
            })
            .collect(Collectors.toList());
        
        data.put("topStudents", topStudents);
        
        return data;
    }
    
    /**
     * 计算维度平均分
     */
    private BigDecimal calculateDimensionAvg(List<StudentProfile> profiles, 
                                             java.util.function.Function<StudentProfile, BigDecimal> getter) {
        if (profiles.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return profiles.stream()
            .map(getter)
            .filter(score -> score != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(profiles.size()), 2, RoundingMode.HALF_UP);
    }
    
    @Override
    public ReportStatusResponse getReportStatus(Long reportId) {
        CourseReport report = courseReportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("报告不存在");
        }
        
        ReportStatusResponse response = new ReportStatusResponse();
        response.setReportId(reportId);
        response.setStatus(report.getFileUrl() != null ? "completed" : "failed");
        response.setFileUrl(report.getFileUrl());
        response.setGenerateTime(report.getGenerateTime());
        return response;
    }
    
    @Override
    public String generateDownloadUrl(Long reportId) {
        String fileUrl = getReportFilePath(reportId);
        return ossFileService.generatePresignedUrl(fileUrl);
    }
    
    @Override
    public void incrementDownloadCount(Long reportId) {
        CourseReport report = courseReportMapper.selectById(reportId);
        if (report != null) {
            report.setDownloadCount(report.getDownloadCount() + 1);
            courseReportMapper.updateById(report);
        }
    }
    
    @Override
    public PageResult<ReportDTO> getCourseReports(Long courseId, Integer pageNum, Integer pageSize) {
        Page<CourseReport> page = new Page<>(pageNum, pageSize);
        Page<CourseReport> reportPage = courseReportMapper.selectPage(page,
            new LambdaQueryWrapper<CourseReport>()
                .eq(CourseReport::getCourseId, courseId)
                .orderByDesc(CourseReport::getGenerateTime)
        );
        
        List<ReportDTO> dtoList = reportPage.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return new PageResult<>(reportPage.getTotal(), dtoList);
    }
    
    @Override
    public PageResult<ReportDTO> getReportList(ReportListRequest request) {
        Page<CourseReport> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<CourseReport> wrapper = new LambdaQueryWrapper<>();
        if (request.getCourseId() != null) {
            wrapper.eq(CourseReport::getCourseId, request.getCourseId());
        }
        if (request.getReportType() != null) {
            wrapper.eq(CourseReport::getReportType, request.getReportType());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(CourseReport::getGenerateTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(CourseReport::getGenerateTime, request.getEndTime());
        }
        wrapper.orderByDesc(CourseReport::getGenerateTime);
        
        Page<CourseReport> reportPage = courseReportMapper.selectPage(page, wrapper);
        
        List<ReportDTO> dtoList = reportPage.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return new PageResult<>(reportPage.getTotal(), dtoList);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReport(Long reportId) {
        CourseReport report = courseReportMapper.selectById(reportId);
        if (report != null) {
            // 删除OSS文件
            if (report.getFileUrl() != null) {
                ossFileService.deleteFile(report.getFileUrl());
            }
            // 删除数据库记录
            courseReportMapper.deleteById(reportId);
            log.info("报告删除成功: reportId={}", reportId);
        }
    }
    
    /**
     * 转换为DTO
     */
    private ReportDTO convertToDTO(CourseReport report) {
        ReportDTO dto = new ReportDTO();
        BeanUtils.copyProperties(report, dto);
        
        // 查询生成人姓名
        if (report.getGeneratorId() != null) {
            UserAccount user = userAccountMapper.selectById(report.getGeneratorId());
            if (user != null && user.getRealName() != null) {
                dto.setGeneratorName(user.getRealName());
            }
        }
        
        return dto;
    }
}

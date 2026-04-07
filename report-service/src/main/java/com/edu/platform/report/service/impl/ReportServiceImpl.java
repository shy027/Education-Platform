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
import com.edu.platform.report.entity.ProfileHistory;
import com.edu.platform.report.entity.StudentProfile;
import com.edu.platform.report.entity.UserAccount;
import com.edu.platform.report.generator.PdfGenerator;
import com.edu.platform.report.mapper.BehaviorLogMapper;
import com.edu.platform.report.mapper.CourseInfoMapper;
import com.edu.platform.report.mapper.CourseReportMapper;
import com.edu.platform.report.mapper.ProfileHistoryMapper;
import com.edu.platform.report.mapper.SchoolReportMapper;
import com.edu.platform.report.mapper.StudentProfileMapper;
import com.edu.platform.report.mapper.UserAccountMapper;
import com.edu.platform.report.service.ConfigService;
import com.edu.platform.report.service.OssFileService;
import com.edu.platform.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
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
    private final SchoolReportMapper schoolReportMapper;
    private final ProfileHistoryMapper profileHistoryMapper;
    private final ConfigService configService;
    private final OssFileService ossFileService;
    private final ObjectMapper objectMapper;

    // 行为类型翻译映射
    private static final Map<String, String> BEHAVIOR_TYPE_MAP = new HashMap<>();
    static {
        BEHAVIOR_TYPE_MAP.put("VIEW_COURSE", "浏览课程");
        BEHAVIOR_TYPE_MAP.put("WATCH_VIDEO", "观看视频");
        BEHAVIOR_TYPE_MAP.put("SUBMIT_TASK", "提交任务");
        BEHAVIOR_TYPE_MAP.put("COURSE_DISCUSSION", "参与讨论");
        BEHAVIOR_TYPE_MAP.put("DO_EXAM", "参加测验");
        BEHAVIOR_TYPE_MAP.put("SIGN_IN", "签到打卡");
        BEHAVIOR_TYPE_MAP.put("POST_COMMENT", "发布评论");
        BEHAVIOR_TYPE_MAP.put("REPLY_COMMENT", "回复评论");
        BEHAVIOR_TYPE_MAP.put("READ_DOC", "阅读文档");
        BEHAVIOR_TYPE_MAP.put("VIEW_DOC", "阅读文档");
        BEHAVIOR_TYPE_MAP.put("LIKE_CONTENT", "内容点赞");
        BEHAVIOR_TYPE_MAP.put("FAVORITE_CONTENT", "收藏内容");
        BEHAVIOR_TYPE_MAP.put("SHARE_CONTENT", "分享内容");
        BEHAVIOR_TYPE_MAP.put("SUBMIT_HOMEWORK", "提交作业");
        BEHAVIOR_TYPE_MAP.put("DO_QUIZ", "参加测验");
    }

    // 行为子词翻译映射 (用于组合翻译)
    private static final Map<String, String> BEHAVIOR_FRAGMENT_MAP = new HashMap<>();
    static {
        BEHAVIOR_FRAGMENT_MAP.put("POST", "发布");
        BEHAVIOR_FRAGMENT_MAP.put("READ", "阅读");
        BEHAVIOR_FRAGMENT_MAP.put("VIEW", "浏览");
        BEHAVIOR_FRAGMENT_MAP.put("SUBMIT", "提交");
        BEHAVIOR_FRAGMENT_MAP.put("TASK", "任务");
        BEHAVIOR_FRAGMENT_MAP.put("DOC", "文档");
        BEHAVIOR_FRAGMENT_MAP.put("COMMENT", "评论");
        BEHAVIOR_FRAGMENT_MAP.put("VIDEO", "视频");
        BEHAVIOR_FRAGMENT_MAP.put("DISCUSSION", "讨论");
        BEHAVIOR_FRAGMENT_MAP.put("LOGIN", "登录");
    }
    
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
    public Long generateSchoolReport(Long schoolId, String startTime, String endTime) {
        try {
            log.info("开始生成学校报告: schoolId={}, period={} to {}", schoolId, startTime, endTime);
            
            // 1. 创建报告记录
            com.edu.platform.report.entity.SchoolReport report = new com.edu.platform.report.entity.SchoolReport();
            report.setSchoolId(schoolId);
            report.setReportTitle("全域思政建设成效报告");
            report.setReportPeriod((startTime != null ? startTime : "不限") + " 至 " + (endTime != null ? endTime : "至今"));
            report.setGeneratorId(com.edu.platform.common.utils.UserContext.getUserId());
            report.setGenerateTime(LocalDateTime.now());
            report.setDownloadCount(0);
            report.setCreatedTime(LocalDateTime.now());
            schoolReportMapper.insert(report);
            
            // 2. 收集数据
            Map<String, Object> data = collectSchoolData(schoolId, startTime, endTime);
            
            // 3. 生成PDF
            byte[] pdfBytes = pdfGenerator.generateSchoolReport(data);
            
            // 4. 上传到OSS
            String fileName = "school_report_" + schoolId + "_" + System.currentTimeMillis() + ".pdf";
            String fileUrl = ossFileService.uploadPdf(pdfBytes, fileName, 0L); // 0L 表示学校层级
            
            // 5. 更新报告记录
            report.setFileUrl(fileUrl);
            schoolReportMapper.updateById(report);
            
            log.info("学校报告生成完成: reportId={}, fileUrl={}", report.getId(), fileUrl);
            return report.getId();
            
        } catch (Exception e) {
            log.error("生成学校报告失败: schoolId={}", schoolId, e);
            throw new RuntimeException("生成学校报告失败: " + e.getMessage());
        }
    }
    
    @Override
    public String getReportFilePath(Long reportId, Integer reportType) {
        // 1. 如果指定了类型，直接查相应表
        if (Integer.valueOf(1).equals(reportType)) {
            CourseReport report = courseReportMapper.selectById(reportId);
            return report != null ? report.getFileUrl() : null;
        } else if (Integer.valueOf(2).equals(reportType)) {
            com.edu.platform.report.entity.SchoolReport schoolReport = schoolReportMapper.selectById(reportId);
            return schoolReport != null ? schoolReport.getFileUrl() : null;
        }
        
        // 2. 如果未指定类型 (兼容旧逻辑)，执行“云路径优先”穿透查询
        // 2.1 优先查找课程表中的云链接
        CourseReport report = courseReportMapper.selectById(reportId);
        if (report != null && report.getFileUrl() != null && report.getFileUrl().startsWith("http")) {
            return report.getFileUrl();
        }
        
        // 2.2 其次查找学校表中的云链接
        com.edu.platform.report.entity.SchoolReport schoolReport = schoolReportMapper.selectById(reportId);
        if (schoolReport != null && schoolReport.getFileUrl() != null && schoolReport.getFileUrl().startsWith("http")) {
            return schoolReport.getFileUrl();
        }
        
        // 3. 兜底策略: 如果都没有云链接，返回第一个非空的记录 (可能是本地路径)
        if (report != null && report.getFileUrl() != null) return report.getFileUrl();
        if (schoolReport != null && schoolReport.getFileUrl() != null) return schoolReport.getFileUrl();
        
        throw new RuntimeException("报告文件记录不存在或链接为空: " + reportId);
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
        
        // --- 增强: 获取全校均线 ---
        if (course != null && course.getTeacherId() != null) {
            Integer schoolId = studentProfileMapper.getSchoolIdByTeacher(course.getTeacherId());
            if (schoolId != null) {
                BigDecimal schoolAvg = studentProfileMapper.getSchoolAvgScore(Long.valueOf(schoolId));
                data.put("schoolAvgScore", schoolAvg != null ? schoolAvg : BigDecimal.ZERO);
            } else {
                data.put("schoolAvgScore", BigDecimal.ZERO);
            }
        } else {
            data.put("schoolAvgScore", BigDecimal.ZERO);
        }

        // --- 增强: 获取历史对比 (30天前) ---
        List<ProfileHistory> history = profileHistoryMapper.selectList(
            new LambdaQueryWrapper<ProfileHistory>()
                .eq(ProfileHistory::getCourseId, courseId)
                .le(ProfileHistory::getSnapshotDate, LocalDate.now().minusDays(30))
                .orderByDesc(ProfileHistory::getSnapshotDate)
        );
        if (!history.isEmpty()) {
            BigDecimal lastAvg = history.stream()
                .map(ProfileHistory::getTotalScore)
                .filter(s -> s != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(history.size()), 2, RoundingMode.HALF_UP);
            data.put("previousAvgScore", lastAvg);
        } else {
            data.put("previousAvgScore", null); // 表示无历史数据
        }

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

        // 增加: 动态获取素养维度名称
        Map<String, String> dimensionNames = configService.getDimensionNames();
        data.put("dimensionNames", dimensionNames);
        
        // 6. 统计行为数据 (并转译为中文)
        List<BehaviorLog> behaviorLogs = behaviorLogMapper.selectList(
            new LambdaQueryWrapper<BehaviorLog>()
                .eq(BehaviorLog::getCourseId, courseId)
                .ge(BehaviorLog::getCreatedTime, LocalDateTime.now().minusDays(30))
        );
        
        Map<String, Integer> behaviorStats = new HashMap<>();
        behaviorLogs.forEach(log -> {
            String type = log.getBehaviorType();
            if (type == null) return;
            
            String normalizedType = type.toUpperCase();
            String chineseType = BEHAVIOR_TYPE_MAP.get(normalizedType);
            
            if (chineseType == null) {
                // 尝试分词翻译 (如 READ_DOC -> 阅读文档)
                String[] parts = normalizedType.split("_");
                StringBuilder sb = new StringBuilder();
                for (String part : parts) {
                    sb.append(BEHAVIOR_FRAGMENT_MAP.getOrDefault(part, part));
                }
                chineseType = sb.toString();
                
                // 如果还是包含英文，做最后的字面清理
                if (chineseType.matches(".*[a-zA-Z].*")) {
                    chineseType = chineseType.toLowerCase()
                        .replace("post", "发布")
                        .replace("comment", "评论")
                        .replace("read", "阅读")
                        .replace("doc", "文档")
                        .replace("task", "任务")
                        .replace("_", " ");
                }
            }
            behaviorStats.merge(chineseType, 1, Integer::sum);
        });
        
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
        
        // --- 增强: 识别预警学生 (按总分升序, 取倒数前3名) ---
        List<Map<String, Object>> warningStudents = profiles.stream()
            .sorted(Comparator.comparing(p -> p.getTotalScore() != null ? p.getTotalScore() : BigDecimal.ZERO))
            .limit(3)
            .map(p -> {
                Map<String, Object> s = new HashMap<>();
                UserAccount user = userAccountMapper.selectById(p.getUserId());
                s.put("userName", user != null ? user.getRealName() : "学生" + p.getUserId());
                s.put("score", p.getTotalScore());
                s.put("reason", "班级综合得分排名靠后(前三)");
                return s;
            })
            .collect(Collectors.toList());
        data.put("warningStudents", warningStudents);

        // --- 增强: 维度过滤与摘要生成 (根据课程勾选的维度) ---
        Map<String, Double> involvedDimensions = new HashMap<>();
        try {
            if (course != null && course.getDimensionWeights() != null) {
                Map<String, Object> weights = objectMapper.readValue(course.getDimensionWeights(), new TypeReference<Map<String, Object>>(){});
                
                if (weights.containsKey("dimension1")) involvedDimensions.put(dimensionNames.getOrDefault("dimension1", "维度1"), ((BigDecimal)data.getOrDefault("dimension1Avg", BigDecimal.ZERO)).doubleValue());
                if (weights.containsKey("dimension2")) involvedDimensions.put(dimensionNames.getOrDefault("dimension2", "维度2"), ((BigDecimal)data.getOrDefault("dimension2Avg", BigDecimal.ZERO)).doubleValue());
                if (weights.containsKey("dimension3")) involvedDimensions.put(dimensionNames.getOrDefault("dimension3", "维度3"), ((BigDecimal)data.getOrDefault("dimension3Avg", BigDecimal.ZERO)).doubleValue());
                if (weights.containsKey("dimension4")) involvedDimensions.put(dimensionNames.getOrDefault("dimension4", "维度4"), ((BigDecimal)data.getOrDefault("dimension4Avg", BigDecimal.ZERO)).doubleValue());
                if (weights.containsKey("dimension5")) involvedDimensions.put(dimensionNames.getOrDefault("dimension5", "维度5"), ((BigDecimal)data.getOrDefault("dimension5Avg", BigDecimal.ZERO)).doubleValue());
            }
        } catch (Exception e) {
            log.warn("解析课程维度权限失败, 默认显示全维度", e);
        }
        
        if (involvedDimensions.isEmpty()) {
            // 如果解析失败或无勾选，默认显示有数据的维度
            if (((BigDecimal)data.getOrDefault("dimension1Avg", BigDecimal.ZERO)).doubleValue() > 0) involvedDimensions.put(dimensionNames.getOrDefault("dimension1", "维度1"), ((BigDecimal)data.getOrDefault("dimension1Avg", BigDecimal.ZERO)).doubleValue());
            if (((BigDecimal)data.getOrDefault("dimension2Avg", BigDecimal.ZERO)).doubleValue() > 0) involvedDimensions.put(dimensionNames.getOrDefault("dimension2", "维度2"), ((BigDecimal)data.getOrDefault("dimension2Avg", BigDecimal.ZERO)).doubleValue());
            if (((BigDecimal)data.getOrDefault("dimension3Avg", BigDecimal.ZERO)).doubleValue() > 0) involvedDimensions.put(dimensionNames.getOrDefault("dimension3", "维度3"), ((BigDecimal)data.getOrDefault("dimension3Avg", BigDecimal.ZERO)).doubleValue());
            if (((BigDecimal)data.getOrDefault("dimension4Avg", BigDecimal.ZERO)).doubleValue() > 0) involvedDimensions.put(dimensionNames.getOrDefault("dimension4", "维度4"), ((BigDecimal)data.getOrDefault("dimension4Avg", BigDecimal.ZERO)).doubleValue());
            if (((BigDecimal)data.getOrDefault("dimension5Avg", BigDecimal.ZERO)).doubleValue() > 0) involvedDimensions.put(dimensionNames.getOrDefault("dimension5", "维度5"), ((BigDecimal)data.getOrDefault("dimension5Avg", BigDecimal.ZERO)).doubleValue());
        }
        data.put("involvedDimensions", involvedDimensions);

        // 生成摘要
        StringBuilder summary = new StringBuilder("本课程重点涉及了 ");
        summary.append(String.join("、", involvedDimensions.keySet()));
        summary.append(" 等核心维度。");
        
        if (!involvedDimensions.isEmpty()) {
            String bestDim = involvedDimensions.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
            summary.append("从得分分布看，班级在“").append(bestDim).append("”维度表现最为突出。");
        }
        data.put("dimensionSummary", summary.toString());

        return data;
    }
    
    /**
     * 收集学校报告数据
     */
    private Map<String, Object> collectSchoolData(Long schoolId, String startTime, String endTime) {
        Map<String, Object> data = new HashMap<>();
        data.put("schoolId", schoolId);
        data.put("reportPeriod", (startTime != null ? startTime : "不限") + " 至 " + (endTime != null ? endTime : "至今"));
        
        // 1. 查询学校所有学生画像
        // 注意: 这里的 SQL 需要根据实际表结构优化，目前假设是通过 generator_id 所在的学校成员表关联
        List<StudentProfile> profiles = studentProfileMapper.selectList(
            new LambdaQueryWrapper<StudentProfile>()
                .apply("user_id IN (SELECT user_id FROM user_school_member WHERE school_id = " + schoolId + ")")
        );
        
        data.put("studentCount", profiles.size());
        data.put("activeStudents", profiles.size()); // 补全缺失的变量
        
        // 2. 统计全校等级分布
        Map<String, Long> levelCounts = profiles.stream()
            .collect(Collectors.groupingBy(
                p -> p.getLevel() != null ? p.getLevel() : "未评定",
                Collectors.counting()
            ));
        
        data.put("excellentCount", levelCounts.getOrDefault("优秀", 0L));
        data.put("goodCount", levelCounts.getOrDefault("良好", 0L));
        data.put("passCount", levelCounts.getOrDefault("合格", 0L));
        data.put("needImprovementCount", levelCounts.getOrDefault("待提升", 0L));
        
        // 3. 计算全校平均分和维度分
        if (!profiles.isEmpty()) {
            BigDecimal avgScore = profiles.stream()
                .map(StudentProfile::getTotalScore)
                .filter(score -> score != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(profiles.size()), 2, RoundingMode.HALF_UP);
            data.put("avgScore", avgScore);
            
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
        
        // 增加: 动态获取素养维度名称
        Map<String, String> dimensionNames = configService.getDimensionNames();
        data.put("dimensionNames", dimensionNames);

        // 4. 查询全校课程数
        Long courseCount = courseInfoMapper.selectCount(
            new LambdaQueryWrapper<CourseInfo>()
                .apply("id IN (SELECT course_id FROM report_student_profile WHERE user_id IN (SELECT user_id FROM user_school_member WHERE school_id = " + schoolId + "))")
        );
        data.put("courseCount", courseCount);
        
        // 5. 统计全校行为数据 (根据时间范围)
        LambdaQueryWrapper<BehaviorLog> behaviorWrapper = new LambdaQueryWrapper<BehaviorLog>()
            .apply("user_id IN (SELECT user_id FROM user_school_member WHERE school_id = " + schoolId + ")");
        
        if (startTime != null) {
            behaviorWrapper.ge(BehaviorLog::getCreatedTime, startTime);
        }
        if (endTime != null) {
            behaviorWrapper.le(BehaviorLog::getCreatedTime, endTime);
        }
        
        List<BehaviorLog> behaviorLogs = behaviorLogMapper.selectList(behaviorWrapper);
        Map<String, Integer> behaviorStats = behaviorLogs.stream()
            .collect(Collectors.groupingBy(
                BehaviorLog::getBehaviorType,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        data.put("behaviorStats", behaviorStats);
        
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
    public String generateDownloadUrl(Long reportId, Integer reportType) {
        String fileUrl = getReportFilePath(reportId, reportType);
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new RuntimeException("无法获取有效的报告路径");
        }
        return ossFileService.generatePresignedUrl(fileUrl);
    }
    
    @Override
    public void incrementDownloadCount(Long reportId, Integer reportType) {
        // 1. 如果指定了类型
        if (Integer.valueOf(1).equals(reportType)) {
            CourseReport report = courseReportMapper.selectById(reportId);
            if (report != null) {
                report.setDownloadCount(report.getDownloadCount() + 1);
                courseReportMapper.updateById(report);
            }
            return;
        } else if (Integer.valueOf(2).equals(reportType)) {
            com.edu.platform.report.entity.SchoolReport schoolReport = schoolReportMapper.selectById(reportId);
            if (schoolReport != null) {
                schoolReport.setDownloadCount(schoolReport.getDownloadCount() + 1);
                schoolReportMapper.updateById(schoolReport);
            }
            return;
        }
        
        // 2. 未指定类型则尝试双表更新
        CourseReport report = courseReportMapper.selectById(reportId);
        if (report != null) {
            report.setDownloadCount(report.getDownloadCount() + 1);
            courseReportMapper.updateById(report);
        }
        
        com.edu.platform.report.entity.SchoolReport schoolReport = schoolReportMapper.selectById(reportId);
        if (schoolReport != null) {
            schoolReport.setDownloadCount(schoolReport.getDownloadCount() + 1);
            schoolReportMapper.updateById(schoolReport);
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
        // 如果是按学校查询学校报告 (Type=2)
        if (Integer.valueOf(2).equals(request.getReportType())) {
            return getSchoolReportPage(request);
        }
        
        // 默认查询课程报告 (或是旧逻辑)
        LambdaQueryWrapper<CourseReport> wrapper = new LambdaQueryWrapper<>();
        if (request.getCourseId() != null) {
            wrapper.eq(CourseReport::getCourseId, request.getCourseId());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(CourseReport::getGenerateTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(CourseReport::getGenerateTime, request.getEndTime());
        }
        if (request.getSchoolId() != null) {
            // 给校领导看该校所有教师生成的报告
            String subQuery = "generator_id IN (SELECT user_id FROM user_school_member WHERE school_id = " + request.getSchoolId() + ")";
            wrapper.apply(subQuery);
        }
        wrapper.orderByDesc(CourseReport::getGenerateTime);
        
        Page<CourseReport> reportPage = courseReportMapper.selectPage(new Page<>(request.getPageNum(), request.getPageSize()), wrapper);
        
        List<ReportDTO> dtoList = reportPage.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return new PageResult<>(reportPage.getTotal(), dtoList);
    }

    /**
     * 分页查询学校报告
     */
    private PageResult<ReportDTO> getSchoolReportPage(ReportListRequest request) {
        LambdaQueryWrapper<com.edu.platform.report.entity.SchoolReport> wrapper = new LambdaQueryWrapper<>();
        if (request.getSchoolId() != null) {
            wrapper.eq(com.edu.platform.report.entity.SchoolReport::getSchoolId, request.getSchoolId());
        }
        wrapper.orderByDesc(com.edu.platform.report.entity.SchoolReport::getGenerateTime);
        
        Page<com.edu.platform.report.entity.SchoolReport> reportPage = schoolReportMapper.selectPage(
            new Page<>(request.getPageNum(), request.getPageSize()), wrapper);
        
        List<ReportDTO> dtoList = reportPage.getRecords().stream()
            .map(this::convertSchoolReportToDTO)
            .collect(Collectors.toList());
            
        return new PageResult<>(reportPage.getTotal(), dtoList);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReport(Long reportId) {
        // 先尝试在课程报告中找
        CourseReport report = courseReportMapper.selectById(reportId);
        if (report != null) {
            if (report.getFileUrl() != null) {
                ossFileService.deleteFile(report.getFileUrl());
            }
            courseReportMapper.deleteById(reportId);
            return;
        }
        
        // 再尝试在学校报告中找
        com.edu.platform.report.entity.SchoolReport schoolReport = schoolReportMapper.selectById(reportId);
        if (schoolReport != null) {
            if (schoolReport.getFileUrl() != null) {
                ossFileService.deleteFile(schoolReport.getFileUrl());
            }
            schoolReportMapper.deleteById(reportId);
            log.info("学校报告删除成功: reportId={}", reportId);
        }
    }
    
    /**
     * 转换为DTO
     */
    private ReportDTO convertToDTO(CourseReport report) {
        ReportDTO dto = new ReportDTO();
        BeanUtils.copyProperties(report, dto);
        dto.setReportType(1); // 课程报告
        dto.setStatus(report.getFileUrl() != null ? 2 : 1);
        dto.setFinishedTime(report.getGenerateTime());
        if (report.getCourseId() != null) {
            CourseInfo course = courseInfoMapper.selectById(report.getCourseId());
            if (course != null) {
                dto.setCourseName(course.getCourseName());
            }
        }
        if (report.getGeneratorId() != null) {
            UserAccount user = userAccountMapper.selectById(report.getGeneratorId());
            if (user != null && user.getRealName() != null) {
                dto.setGeneratorName(user.getRealName());
            }
        }
        return dto;
    }

    /**
     * 学校报告转换为DTO
     */
    private ReportDTO convertSchoolReportToDTO(com.edu.platform.report.entity.SchoolReport report) {
        ReportDTO dto = new ReportDTO();
        BeanUtils.copyProperties(report, dto);
        dto.setReportType(2); // 学校报告
        dto.setStatus(report.getFileUrl() != null ? 2 : 1);
        dto.setFinishedTime(report.getGenerateTime());
        if (report.getGeneratorId() != null) {
            UserAccount user = userAccountMapper.selectById(report.getGeneratorId());
            if (user != null) {
                dto.setGeneratorName(user.getRealName());
            }
        }
        return dto;
    }
}

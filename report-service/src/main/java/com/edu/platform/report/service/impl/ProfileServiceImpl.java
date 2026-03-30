package com.edu.platform.report.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.report.calculator.ProfileCalculator;
import com.edu.platform.report.dto.response.GrowthTrackResponse;
import com.edu.platform.report.dto.response.RadarDataResponse;
import com.edu.platform.report.dto.response.StatisticsResponse;
import com.edu.platform.report.entity.BehaviorLog;
import com.edu.platform.report.entity.ProfileHistory;
import com.edu.platform.report.entity.StudentProfile;
import com.edu.platform.report.mapper.BehaviorLogMapper;
import com.edu.platform.report.mapper.ProfileHistoryMapper;
import com.edu.platform.report.mapper.StudentProfileMapper;
import com.edu.platform.report.service.ConfigService;
import com.edu.platform.report.service.ProfileService;
import com.edu.platform.report.client.ResourceClient;
import com.edu.platform.report.dto.ResourceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 素养画像服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    
    private final BehaviorLogMapper behaviorLogMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final ProfileHistoryMapper profileHistoryMapper;
    private final ProfileCalculator profileCalculator;
    private final ConfigService configService;
    private final ResourceClient resourceClient;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateProfile(Long userId, Long courseId) {
        try {
            log.info("开始计算素养画像: userId={}, courseId={}", userId, courseId);
            
            // 校验：仅为学生计算画像
            Integer memberType = studentProfileMapper.getMemberType(userId);
            if (memberType == null || memberType != 3) {
                log.info("用户非学生角色(memberType={}), 跳过画像计算: userId={}", memberType, userId);
                return;
            }
            
            // 1. 获取配置
            String behaviorWeights = configService.getBehaviorWeights();
            Map<String, BigDecimal> dimensionWeights = configService.getDimensionWeights();
            Map<String, BigDecimal> levelThresholds = configService.getLevelThresholds();
            
            if (behaviorWeights == null) {
                log.error("未找到行为权重配置,跳过计算");
                return;
            }
            
            // 2. 查询用户的学习行为记录 (扩展至 365 天，确保画像连续性)
            LocalDateTime startTime = LocalDateTime.now().minusDays(365);
            LambdaQueryWrapper<BehaviorLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BehaviorLog::getUserId, userId);
            // 如果 courseId 不为 0，则按课程过滤；若为 0，则查全平台行为（包含 course_id=0 的资源浏览和各课程行为）
            if (courseId != null && courseId != 0) {
                wrapper.eq(BehaviorLog::getCourseId, courseId);
            }
            wrapper.ge(BehaviorLog::getCreatedTime, startTime)
                   .orderByDesc(BehaviorLog::getCreatedTime);
            
            List<BehaviorLog> behaviorLogs = behaviorLogMapper.selectList(wrapper);
            
            if (behaviorLogs.isEmpty()) {
                log.info("用户暂无学习行为记录: userId={}, courseId={}", userId, courseId);
                return;
            }
            
            // 3. 计算六维度得分
            // 3.1 获取双通道配置
            Map<String, BigDecimal> scoreConfig = configService.getScoreConfig();
            Map<String, Object> tagWeights = configService.getResourceTagWeights();
            
            // 3.2 收集待查询标签的资源ID (兼容多种浏览行为类型)
            List<Long> resourceIds = behaviorLogs.stream()
                    .filter(log -> {
                        String type = log.getBehaviorType();
                        return "RESOURCE_VIEW".equals(type) || "WATCH_VIDEO".equals(type) || "READ_DOC".equals(type);
                    })
                    .map(BehaviorLog::getBehaviorObjectId)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            
            // 3.3 批量获取资源标签映射
            Map<Long, List<String>> resourceTagsMap = new HashMap<>();
            if (!resourceIds.isEmpty()) {
                com.edu.platform.common.result.Result<List<ResourceResponse>> result = resourceClient.getResourcesByIds(resourceIds);
                if (result.getCode() == 200 && result.getData() != null) {
                    for (ResourceResponse res : result.getData()) {
                        if (res.getTags() != null) {
                            List<String> tags = res.getTags().stream()
                                    .map(ResourceResponse.TagInfo::getTagName)
                                    .collect(java.util.stream.Collectors.toList());
                            resourceTagsMap.put(res.getId(), tags);
                        }
                    }
                }
            }

            // 3.4 调用综合计算器
            log.info("用户行为记录数: {}, 准备计算画像...", behaviorLogs.size());
            Map<String, BigDecimal> dimensionScores = profileCalculator.calculateDimensionScores(
                    behaviorLogs, resourceTagsMap, behaviorWeights, scoreConfig, tagWeights);
            
            log.info("画像计算完成, 原始维度分: {}", dimensionScores);
            
            // 4. 计算综合得分
            BigDecimal totalScore = profileCalculator.calculateTotalScore(dimensionScores, dimensionWeights);
            
            // 5. 评定等级
            String level = profileCalculator.evaluateLevel(totalScore, levelThresholds);
            
            // 6. 查询上次画像,判断成长趋势
            StudentProfile previousProfile = studentProfileMapper.selectOne(
                new LambdaQueryWrapper<StudentProfile>()
                    .eq(StudentProfile::getUserId, userId)
                    .eq(StudentProfile::getCourseId, courseId)
            );
            
            String trend = "稳定";
            if (previousProfile != null) {
                trend = profileCalculator.evaluateTrend(totalScore, previousProfile.getTotalScore());
            }
            
            // 7. 更新或插入画像记录
            StudentProfile profile = new StudentProfile();
            if (previousProfile != null) {
                profile.setId(previousProfile.getId());
            }
            profile.setUserId(userId);
            profile.setCourseId(courseId);
            profile.setDimension1Score(dimensionScores.get("dimension1"));
            profile.setDimension2Score(dimensionScores.get("dimension2"));
            profile.setDimension3Score(dimensionScores.get("dimension3"));
            profile.setDimension4Score(dimensionScores.get("dimension4"));
            profile.setDimension5Score(dimensionScores.get("dimension5"));
            profile.setTotalScore(totalScore);
            profile.setLevel(level);
            profile.setGrowthTrend(trend);
            
            if (previousProfile != null) {
                studentProfileMapper.updateById(profile);
            } else {
                studentProfileMapper.insert(profile);
            }
            
            // 8. 保存历史快照
            saveProfileHistory(userId, courseId, dimensionScores, totalScore);
            
            log.info("素养画像计算完成: userId={}, courseId={}, totalScore={}, level={}", 
                    userId, courseId, totalScore, level);
            
        } catch (Exception e) {
            log.error("计算素养画像失败: userId={}, courseId={}", userId, courseId, e);
            throw e;
        }
    }
    
    @Override
    public void calculateAllProfiles(Long courseId) {
        // 查询有行为记录的用户
        LambdaQueryWrapper<BehaviorLog> wrapper = new LambdaQueryWrapper<BehaviorLog>()
                .select(BehaviorLog::getUserId)
                .apply("user_id IN (SELECT user_id FROM user_school_member WHERE member_type = 3)");
        if (courseId != null && courseId != 0) {
            wrapper.eq(BehaviorLog::getCourseId, courseId);
        }
        wrapper.groupBy(BehaviorLog::getUserId);
        
        List<Long> userIds = behaviorLogMapper.selectObjs(wrapper);
        
        log.info("开始批量计算素养画像: courseId={}, userCount={}", courseId, userIds.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (Long userId : userIds) {
            try {
                calculateProfile(userId, courseId);
                successCount++;
            } catch (Exception e) {
                log.error("计算用户画像失败: userId={}", userId, e);
                failCount++;
            }
        }
        
        log.info("批量计算完成: courseId={}, 成功={}, 失败={}", courseId, successCount, failCount);
    }
    
    @Override
    public Map<String, Object> getProfile(Long userId, Long courseId) {
        StudentProfile profile = studentProfileMapper.selectOne(
            new LambdaQueryWrapper<StudentProfile>()
                .eq(StudentProfile::getUserId, userId)
                .eq(StudentProfile::getCourseId, courseId)
        );
        
        Map<String, Object> result = new HashMap<>();
        
        if (profile == null) {
            result.put("exists", false);
            return result;
        }
        
        result.put("exists", true);
        result.put("userId", profile.getUserId());
        result.put("courseId", profile.getCourseId());
        result.put("dimension1Score", profile.getDimension1Score());
        result.put("dimension2Score", profile.getDimension2Score());
        result.put("dimension3Score", profile.getDimension3Score());
        result.put("dimension4Score", profile.getDimension4Score());
        result.put("dimension5Score", profile.getDimension5Score());
        result.put("totalScore", profile.getTotalScore());
        result.put("profileLevel", profile.getLevel());
        result.put("level", profile.getLevel());
        result.put("growthTrend", profile.getGrowthTrend());
        result.put("updatedTime", profile.getUpdatedTime());
        
        return result;
    }
    
    /**
     * 保存画像历史快照
     */
    private void saveProfileHistory(Long userId, Long courseId, Map<String, BigDecimal> dimensionScores, BigDecimal totalScore) {
        ProfileHistory history = new ProfileHistory();
        history.setUserId(userId);
        history.setCourseId(courseId);
        history.setDimension1Score(dimensionScores.get("dimension1"));
        history.setDimension2Score(dimensionScores.get("dimension2"));
        history.setDimension3Score(dimensionScores.get("dimension3"));
        history.setDimension4Score(dimensionScores.get("dimension4"));
        history.setDimension5Score(dimensionScores.get("dimension5"));
        history.setTotalScore(totalScore);
        history.setSnapshotDate(LocalDate.now());
        
        // 检查今天是否已有快照
        ProfileHistory existing = profileHistoryMapper.selectOne(
            new LambdaQueryWrapper<ProfileHistory>()
                .eq(ProfileHistory::getUserId, userId)
                .eq(ProfileHistory::getCourseId, courseId)
                .eq(ProfileHistory::getSnapshotDate, LocalDate.now())
        );
        
        if (existing != null) {
            // 更新今天的快照
            history.setId(existing.getId());
            profileHistoryMapper.updateById(history);
        } else {
            // 插入新快照
            profileHistoryMapper.insert(history);
        }
    }
    
    @Override
    public RadarDataResponse getRadarData(Long userId, Long courseId) {
        StudentProfile profile = studentProfileMapper.selectOne(
            new LambdaQueryWrapper<StudentProfile>()
                .eq(StudentProfile::getUserId, userId)
                .eq(StudentProfile::getCourseId, courseId)
        );
        
        RadarDataResponse response = new RadarDataResponse();
        response.setUserId(userId);
        response.setCourseId(courseId);
        
        if (profile != null) {
            response.setTotalScore(profile.getTotalScore());
            response.setLevel(profile.getLevel());
            response.setGrowthTrend(profile.getGrowthTrend());
            response.setUpdatedTime(profile.getUpdatedTime());
        }
        
        // 构建六维度数据 (即使没有画像数据也返回框架,以便前端展示空雷达图)
        Map<String, String> dimensionNames = configService.getDimensionNames();
        List<RadarDataResponse.DimensionData> dimensions = new java.util.ArrayList<>();
        dimensions.add(new RadarDataResponse.DimensionData(dimensionNames.getOrDefault("dimension1", "知识技能素养"), profile != null ? profile.getDimension1Score() : java.math.BigDecimal.ZERO));
        dimensions.add(new RadarDataResponse.DimensionData(dimensionNames.getOrDefault("dimension2", "职业品格素养"), profile != null ? profile.getDimension2Score() : java.math.BigDecimal.ZERO));
        dimensions.add(new RadarDataResponse.DimensionData(dimensionNames.getOrDefault("dimension3", "创新实践素养"), profile != null ? profile.getDimension3Score() : java.math.BigDecimal.ZERO));
        dimensions.add(new RadarDataResponse.DimensionData(dimensionNames.getOrDefault("dimension4", "社会责任素养"), profile != null ? profile.getDimension4Score() : java.math.BigDecimal.ZERO));
        dimensions.add(new RadarDataResponse.DimensionData(dimensionNames.getOrDefault("dimension5", "发展适应素养"), profile != null ? profile.getDimension5Score() : java.math.BigDecimal.ZERO));
        response.setDimensions(dimensions);
        
        return response;
    }
    
    @Override
    public GrowthTrackResponse getGrowthTrack(Long userId, Long courseId, Integer days) {
        if (days == null || days <= 0) {
            days = 30;
        }
        
        // 查询历史快照
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        LambdaQueryWrapper<ProfileHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProfileHistory::getUserId, userId)
               .eq(ProfileHistory::getCourseId, courseId)
               .ge(ProfileHistory::getSnapshotDate, startDate)
               .orderByAsc(ProfileHistory::getSnapshotDate);
        
        List<ProfileHistory> historyList = profileHistoryMapper.selectList(wrapper);
        
        GrowthTrackResponse response = new GrowthTrackResponse();
        response.setUserId(userId);
        response.setCourseId(courseId);
        
        // 构建轨迹数据点
        List<GrowthTrackResponse.TrackPoint> trackData = new java.util.ArrayList<>();
        for (ProfileHistory history : historyList) {
            GrowthTrackResponse.TrackPoint point = new GrowthTrackResponse.TrackPoint();
            point.setDate(history.getSnapshotDate());
            point.setTotalScore(history.getTotalScore());
            point.setDimension1Score(history.getDimension1Score());
            point.setDimension2Score(history.getDimension2Score());
            point.setDimension3Score(history.getDimension3Score());
            point.setDimension4Score(history.getDimension4Score());
            point.setDimension5Score(history.getDimension5Score());
            trackData.add(point);
        }
        response.setTrackData(trackData);
        
        // 计算成长趋势和进步幅度
        if (trackData.size() >= 2) {
            BigDecimal firstScore = trackData.get(0).getTotalScore();
            BigDecimal lastScore = trackData.get(trackData.size() - 1).getTotalScore();
            BigDecimal improvement = lastScore.subtract(firstScore);
            
            response.setImprovement(improvement);
            if (improvement.compareTo(BigDecimal.ZERO) > 0) {
                response.setTrend("上升");
            } else if (improvement.compareTo(BigDecimal.ZERO) < 0) {
                response.setTrend("下降");
            } else {
                response.setTrend("稳定");
            }
        } else {
            response.setTrend("稳定");
            response.setImprovement(BigDecimal.ZERO);
        }
        
        return response;
    }
    
    @Override
    public IPage<StudentProfile> listProfiles(Page<StudentProfile> page, Long courseId, Long schoolId, String department, String className) {
        LambdaQueryWrapper<StudentProfile> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null && courseId != 0) {
            wrapper.eq(StudentProfile::getCourseId, courseId);
        }
        
        // 强制仅显示学生 (member_type = 3)
        StringBuilder subQuery = new StringBuilder("user_id IN (SELECT user_id FROM user_school_member WHERE member_type = 3");
        if (schoolId != null) {
            subQuery.append(" AND school_id = ").append(schoolId);
        }
        if (StrUtil.isNotBlank(department)) {
            subQuery.append(" AND department = '").append(department).append("'");
        }
        if (StrUtil.isNotBlank(className)) {
            subQuery.append(" AND class_name = '").append(className).append("'");
        }
        subQuery.append(")");
        wrapper.apply(subQuery.toString());
        
        wrapper.orderByDesc(StudentProfile::getTotalScore);
        return studentProfileMapper.selectPage(page, wrapper);
    }
    
    @Override
    public StatisticsResponse getStatistics(Long userId, Long courseId, Integer days) {
        if (days == null || days <= 0) {
            days = 30;
        }
        
        // 查询行为记录
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<BehaviorLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BehaviorLog::getUserId, userId);
        if (courseId != null && courseId != 0) {
            wrapper.eq(BehaviorLog::getCourseId, courseId);
        }
        wrapper.ge(BehaviorLog::getCreatedTime, startTime)
               .orderByDesc(BehaviorLog::getCreatedTime);
        
        List<BehaviorLog> behaviorLogs = behaviorLogMapper.selectList(wrapper);
        
        StatisticsResponse response = new StatisticsResponse();
        response.setUserId(userId);
        response.setCourseId(courseId);
        
        // 设置统计周期
        StatisticsResponse.Period period = new StatisticsResponse.Period();
        period.setStartDate(startTime.toLocalDate());
        period.setEndDate(LocalDate.now());
        period.setDays(days);
        response.setPeriod(period);
        
        // 按行为类型分组统计
        Map<String, StatisticsResponse.BehaviorStat> behaviorStats = new HashMap<>();
        Map<String, List<BehaviorLog>> groupedLogs = new HashMap<>();
        
        for (BehaviorLog log : behaviorLogs) {
            String behaviorType = log.getBehaviorType();
            groupedLogs.computeIfAbsent(behaviorType, k -> new java.util.ArrayList<>()).add(log);
        }
        
        for (Map.Entry<String, List<BehaviorLog>> entry : groupedLogs.entrySet()) {
            String behaviorType = entry.getKey();
            List<BehaviorLog> logs = entry.getValue();
            
            StatisticsResponse.BehaviorStat stat = new StatisticsResponse.BehaviorStat();
            stat.setCount(logs.size());
            
            // 计算去重后的行为次数 (针对同一个研讨/作业)
            long uniqueCount = logs.stream()
                .map(log -> {
                    // 对于讨论话题，如果 behaviorData 中有 postId，则按 postId 去重（同一话题下的发帖和回复算一次）
                    if ("POST_COMMENT".equals(log.getBehaviorType()) && log.getBehaviorData() != null) {
                        try {
                            cn.hutool.json.JSONObject data = cn.hutool.json.JSONUtil.parseObj(log.getBehaviorData());
                            if (Boolean.FALSE.equals(data.getBool("isPost")) && data.containsKey("postId")) {
                                return data.getLong("postId");
                            }
                        } catch (Exception e) {
                            // 忽略解析错误，回退到 behaviorObjectId
                        }
                    }
                    return log.getBehaviorObjectId();
                })
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();
            stat.setUniqueCount((int) uniqueCount);
            
            // 根据行为类型计算不同的统计数据
            if ("VIEW_COURSEWARE".equals(behaviorType) || "VIEW_CASE".equals(behaviorType) || "WATCH_VIDEO".equals(behaviorType)) {
                int totalDuration = logs.stream()
                    .filter(log -> log.getDurationSeconds() != null)
                    .mapToInt(BehaviorLog::getDurationSeconds)
                    .sum();
                stat.setTotalDuration(totalDuration);
            }
            
            behaviorStats.put(behaviorType, stat);
        }
        response.setBehaviorStats(behaviorStats);
        
        // 计算汇总数据
        StatisticsResponse.Summary summary = new StatisticsResponse.Summary();
        summary.setTotalBehaviors(behaviorLogs.size());
        
        // 计算活跃天数
        long activeDays = behaviorLogs.stream()
            .map(log -> log.getCreatedTime().toLocalDate())
            .distinct()
            .count();
        summary.setActiveDays((int) activeDays);

        // 计算参与课程数量
        long participatedCourses = behaviorLogs.stream()
            .map(BehaviorLog::getCourseId)
            .filter(id -> id != null && id != 0)
            .distinct()
            .count();
        summary.setParticipatedCourses((int) participatedCourses);
        
        // 计算日均行为次数
        if (activeDays > 0) {
            summary.setAvgDailyBehaviors((double) behaviorLogs.size() / activeDays);
        } else {
            summary.setAvgDailyBehaviors(0.0);
        }
        
        // 计算最活跃时段
        Map<Integer, Long> hourCounts = behaviorLogs.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                log -> log.getCreatedTime().getHour(),
                java.util.stream.Collectors.counting()
            ));
        
        if (!hourCounts.isEmpty()) {
            summary.setMostActiveHour(
                hourCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(0)
            );
        } else {
            summary.setMostActiveHour(0);
        }
        
        response.setSummary(summary);
        
        return response;
    }
    
}

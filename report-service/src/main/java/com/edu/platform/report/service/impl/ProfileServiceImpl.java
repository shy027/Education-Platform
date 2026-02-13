package com.edu.platform.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateProfile(Long userId, Long courseId) {
        try {
            log.info("开始计算素养画像: userId={}, courseId={}", userId, courseId);
            
            // 1. 获取权重配置
            String weights = configService.getProfileWeights();
            if (weights == null) {
                log.error("未找到权重配置,跳过计算");
                return;
            }
            
            // 2. 查询用户的学习行为记录(最近30天)
            LocalDateTime startTime = LocalDateTime.now().minusDays(30);
            LambdaQueryWrapper<BehaviorLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BehaviorLog::getUserId, userId)
                   .eq(BehaviorLog::getCourseId, courseId)
                   .ge(BehaviorLog::getCreatedTime, startTime)
                   .orderByDesc(BehaviorLog::getCreatedTime);
            
            List<BehaviorLog> behaviorLogs = behaviorLogMapper.selectList(wrapper);
            
            if (behaviorLogs.isEmpty()) {
                log.info("用户暂无学习行为记录: userId={}, courseId={}", userId, courseId);
                return;
            }
            
            // 3. 计算五维度得分
            Map<String, BigDecimal> dimensionScores = profileCalculator.calculateDimensionScores(behaviorLogs, weights);
            
            // 4. 计算综合得分
            BigDecimal totalScore = profileCalculator.calculateTotalScore(dimensionScores, weights);
            
            // 5. 评定等级
            String level = profileCalculator.evaluateLevel(totalScore, weights);
            
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
        // 查询该课程下所有有行为记录的用户
        List<Long> userIds = behaviorLogMapper.selectObjs(
            new LambdaQueryWrapper<BehaviorLog>()
                .select(BehaviorLog::getUserId)
                .eq(BehaviorLog::getCourseId, courseId)
                .groupBy(BehaviorLog::getUserId)
        );
        
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
        
        if (profile == null) {
            return null;
        }
        
        RadarDataResponse response = new RadarDataResponse();
        response.setUserId(userId);
        response.setCourseId(courseId);
        response.setTotalScore(profile.getTotalScore());
        response.setLevel(profile.getLevel());
        response.setGrowthTrend(profile.getGrowthTrend());
        response.setUpdatedTime(profile.getUpdatedTime());
        
        // 构建五维度数据
        List<RadarDataResponse.DimensionData> dimensions = new java.util.ArrayList<>();
        dimensions.add(new RadarDataResponse.DimensionData("价值观认同", profile.getDimension1Score()));
        dimensions.add(new RadarDataResponse.DimensionData("思想品德", profile.getDimension2Score()));
        dimensions.add(new RadarDataResponse.DimensionData("社会责任", profile.getDimension3Score()));
        dimensions.add(new RadarDataResponse.DimensionData("创新精神", profile.getDimension4Score()));
        dimensions.add(new RadarDataResponse.DimensionData("团队协作", profile.getDimension5Score()));
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
        List<ProfileHistory> historyList = profileHistoryMapper.selectList(
            new LambdaQueryWrapper<ProfileHistory>()
                .eq(ProfileHistory::getUserId, userId)
                .eq(ProfileHistory::getCourseId, courseId)
                .ge(ProfileHistory::getSnapshotDate, startDate)
                .orderByAsc(ProfileHistory::getSnapshotDate)
        );
        
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
    public StatisticsResponse getStatistics(Long userId, Long courseId, Integer days) {
        if (days == null || days <= 0) {
            days = 30;
        }
        
        // 查询行为记录
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<BehaviorLog> behaviorLogs = behaviorLogMapper.selectList(
            new LambdaQueryWrapper<BehaviorLog>()
                .eq(BehaviorLog::getUserId, userId)
                .eq(BehaviorLog::getCourseId, courseId)
                .ge(BehaviorLog::getCreatedTime, startTime)
                .orderByDesc(BehaviorLog::getCreatedTime)
        );
        
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
            
            // 根据行为类型计算不同的统计数据
            if ("VIEW_COURSEWARE".equals(behaviorType) || "VIEW_CASE".equals(behaviorType)) {
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

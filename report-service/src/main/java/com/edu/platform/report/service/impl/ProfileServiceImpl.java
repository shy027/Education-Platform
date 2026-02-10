package com.edu.platform.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.platform.report.calculator.ProfileCalculator;
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
    
}

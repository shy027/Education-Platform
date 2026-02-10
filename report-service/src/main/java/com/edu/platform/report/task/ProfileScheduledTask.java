package com.edu.platform.report.task;

import com.edu.platform.report.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 素养画像定时任务
 *
 * @author Education Platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileScheduledTask {
    
    private final ProfileService profileService;
    
    /**
     * 每日凌晨2点计算所有用户的素养画像
     * Cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void calculateDailyProfiles() {
        log.info("开始执行每日素养画像计算任务");
        
        try {
            // TODO: 查询所有活跃课程ID
            // 这里暂时硬编码一个课程ID作为示例
            // 实际应该查询所有有学习行为的课程
            Long courseId = 1001L;
            
            profileService.calculateAllProfiles(courseId);
            
            log.info("每日素养画像计算任务完成");
        } catch (Exception e) {
            log.error("每日素养画像计算任务失败", e);
        }
    }
    
    /**
     * 手动触发画像计算(用于测试)
     * 可通过管理接口调用
     */
    public void triggerCalculation(Long courseId) {
        log.info("手动触发素养画像计算: courseId={}", courseId);
        profileService.calculateAllProfiles(courseId);
    }
    
}

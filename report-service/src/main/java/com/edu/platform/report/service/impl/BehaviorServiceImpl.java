package com.edu.platform.report.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.report.dto.request.BehaviorLogRequest;
import com.edu.platform.report.entity.BehaviorLog;
import com.edu.platform.report.mapper.BehaviorLogMapper;
import com.edu.platform.report.service.BehaviorService;
import com.edu.platform.report.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 行为埋点服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BehaviorServiceImpl implements BehaviorService {
    
    private final BehaviorLogMapper behaviorLogMapper;
    private final HttpServletRequest request;
    private final @Lazy ProfileService profileService;
    
    @jakarta.annotation.Resource
    private @Lazy BehaviorService self;
    
    @Override
    public void logBehavior(BehaviorLogRequest behaviorRequest) {
        log.info("收到行为埋点原始请求: {}", behaviorRequest);
        try {
            // 优先使用请求中指定的 userId (用于跨服务代发，如教师加精)
            Long userId = behaviorRequest.getUserId();
            if (userId == null) {
                userId = UserContext.getUserId();
            }
            log.info("收到的行为埋点处理: targetUserId={}, request={}", userId, behaviorRequest);
            
            // 创建行为日志实体
            BehaviorLog behaviorLog = new BehaviorLog();
            BeanUtil.copyProperties(behaviorRequest, behaviorLog);
            
            // 补充字段 (以防复制失败)
            if (behaviorLog.getBehaviorObjectId() == null) {
                behaviorLog.setBehaviorObjectId(behaviorRequest.getBehaviorObjectId());
            }

            log.info("转换后的实体对象: behaviorObjectId={}, behaviorType={}", 
                    behaviorLog.getBehaviorObjectId(), behaviorLog.getBehaviorType());
            
            // 设置用户ID
            if (userId == null) {
                // 如果依然获取不到用户ID，说明是匿名操作或校验失败
                log.debug("无法获取用户ID,跳过行为埋点记录");
                return;
            }
            
            behaviorLog.setUserId(userId);
            
            // 设置IP地址和User-Agent
            behaviorLog.setIpAddress(getClientIp());
            behaviorLog.setUserAgent(request.getHeader("User-Agent"));
            
            // 保存到数据库
            behaviorLogMapper.insert(behaviorLog);
            
            log.info("行为埋点记录成功: userId={}, behaviorType={}, courseId={}", 
                    userId, behaviorRequest.getBehaviorType(), behaviorRequest.getCourseId());
            
            // 实时触发画像计算 (通过代理调用确保 @Async 生效)
            self.triggerCalculate(userId, behaviorRequest.getCourseId());
            
        } catch (Exception e) {
            // 行为埋点失败不影响主业务流程,只记录日志
            log.error("行为埋点记录失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 异步触发画像计算
     */
    @Async
    public void triggerCalculate(Long userId, Long courseId) {
        try {
            // 1. 计算当前课程画像
            if (courseId != null && courseId != 0) {
                profileService.calculateProfile(userId, courseId);
            }
            // 2. 始终触发全局画像计算 (courseId=0)
            profileService.calculateProfile(userId, 0L);
        } catch (Exception e) {
            log.error("异步触发画像计算失败: userId={}", userId, e);
        }
    }
    
    @Override
    public void deleteBehavior(String type, Long objectId) {
        log.info("开始删除行为记录: type={}, objectId={}", type, objectId);
        try {
            // 查找受影响的用户和课程，用于后续重计
            java.util.List<BehaviorLog> logs = behaviorLogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BehaviorLog>()
                    .eq(BehaviorLog::getBehaviorType, type)
                    .eq(BehaviorLog::getBehaviorObjectId, objectId)
            );

            if (logs.isEmpty()) {
                log.info("未找到匹配的行为记录, type={}, objectId={}", type, objectId);
                return;
            }

            // 删除记录
            behaviorLogMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BehaviorLog>()
                    .eq(BehaviorLog::getBehaviorType, type)
                    .eq(BehaviorLog::getBehaviorObjectId, objectId)
            );

            log.info("已删除 {} 条行为记录", logs.size());

            // 针对每个受影响的用户触发画像更新
            for (BehaviorLog logEntry : logs) {
                if (logEntry.getUserId() != null) {
                    self.triggerCalculate(logEntry.getUserId(), logEntry.getCourseId());
                }
            }
        } catch (Exception e) {
            log.error("删除行为记录失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况,取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
}

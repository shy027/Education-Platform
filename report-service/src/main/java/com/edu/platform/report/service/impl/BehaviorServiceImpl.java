package com.edu.platform.report.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.report.dto.request.BehaviorLogRequest;
import com.edu.platform.report.entity.BehaviorLog;
import com.edu.platform.report.mapper.BehaviorLogMapper;
import com.edu.platform.report.service.BehaviorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
    @Override
    public void logBehavior(BehaviorLogRequest behaviorRequest) {
        try {
            // 创建行为日志实体
            BehaviorLog behaviorLog = new BehaviorLog();
            BeanUtil.copyProperties(behaviorRequest, behaviorLog);
            
            // 设置用户ID(从UserContext获取,由UserInterceptor自动设置)
            Long userId = UserContext.getUserId();
            if (userId == null) {
                // 未登录用户,跳过埋点记录(不影响主业务)
                log.debug("用户未登录,跳过行为埋点记录");
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
        } catch (Exception e) {
            // 行为埋点失败不影响主业务流程,只记录日志
            log.error("行为埋点记录失败: {}", e.getMessage(), e);
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

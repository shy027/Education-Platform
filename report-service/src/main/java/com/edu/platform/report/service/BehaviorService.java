package com.edu.platform.report.service;

import com.edu.platform.report.dto.request.BehaviorLogRequest;

/**
 * 行为埋点服务接口
 *
 * @author Education Platform
 */
public interface BehaviorService {
    
    /**
     * 记录学习行为
     *
     * @param request 行为埋点请求
     */
    void logBehavior(BehaviorLogRequest request);
    
}

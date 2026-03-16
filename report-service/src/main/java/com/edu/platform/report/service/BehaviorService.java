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

    /**
     * 异步触发画像计算
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     */
    void triggerCalculate(Long userId, Long courseId);

    /**
     * 删除行为记录
     *
     * @param type 类型
     * @param objectId 对象ID
     */
    void deleteBehavior(String type, Long objectId);
}

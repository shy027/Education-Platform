package com.edu.platform.report.service;

import java.util.Map;

/**
 * 素养画像服务接口
 *
 * @author Education Platform
 */
public interface ProfileService {
    
    /**
     * 计算指定用户和课程的素养画像
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     */
    void calculateProfile(Long userId, Long courseId);
    
    /**
     * 批量计算所有用户的素养画像
     *
     * @param courseId 课程ID
     */
    void calculateAllProfiles(Long courseId);
    
    /**
     * 获取用户的素养画像数据
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 画像数据
     */
    Map<String, Object> getProfile(Long userId, Long courseId);
    
}

package com.edu.platform.report.service;

import com.edu.platform.report.dto.response.GrowthTrackResponse;
import com.edu.platform.report.dto.response.RadarDataResponse;
import com.edu.platform.report.dto.response.StatisticsResponse;

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
    
    /**
     * 获取雷达图数据
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 雷达图数据
     */
    RadarDataResponse getRadarData(Long userId, Long courseId);
    
    /**
     * 获取成长轨迹
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param days 天数(默认30天)
     * @return 成长轨迹数据
     */
    GrowthTrackResponse getGrowthTrack(Long userId, Long courseId, Integer days);
    
    /**
     * 获取学习统计
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param days 天数(默认30天)
     * @return 学习统计数据
     */
    StatisticsResponse getStatistics(Long userId, Long courseId, Integer days);
    
}

package com.edu.platform.report.service;

import com.edu.platform.report.dto.ConfigDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 *
 * @author Education Platform
 */
public interface ConfigService {
    
    /**
     * 获取配置值
     *
     * @param configKey 配置键
     * @return 配置值(JSON字符串)
     */
    String getConfigValue(String configKey);
    
    /**
     * 更新配置值
     *
     * @param configKey 配置键
     * @param configValue 配置值
     */
    void updateConfig(String configKey, String configValue);
    
    /**
     * 获取画像权重配置
     *
     * @return 权重配置(JSON字符串)
     */
    String getProfileWeights();
    
    /**
     * 获取维度权重配置(Map格式)
     *
     * @return 维度权重Map
     */
    Map<String, BigDecimal> getDimensionWeights();
    
    /**
     * 更新维度权重配置
     *
     * @param weights 权重Map
     */
    void updateDimensionWeights(Map<String, BigDecimal> weights);
    
    /**
     * 获取优秀阈值
     *
     * @return 优秀阈值
     */
    BigDecimal getExcellentThreshold();
    
    /**
     * 获取良好阈值
     *
     * @return 良好阈值
     */
    BigDecimal getGoodThreshold();
    
    /**
     * 获取合格阈值
     *
     * @return 合格阈值
     */
    BigDecimal getPassThreshold();
    
    /**
     * 更新等级阈值
     *
     * @param thresholds 阈值Map(excellent, good, pass)
     */
    void updateLevelThresholds(Map<String, BigDecimal> thresholds);
    
    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    List<ConfigDTO> getAllConfigs();
    
    /**
     * 刷新配置缓存
     *
     * @param configKey 配置键
     */
    void refreshCache(String configKey);
}

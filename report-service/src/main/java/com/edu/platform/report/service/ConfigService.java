package com.edu.platform.report.service;

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
     * 获取画像权重配置
     *
     * @return 权重配置(JSON字符串)
     */
    String getProfileWeights();
    
    /**
     * 刷新配置缓存
     *
     * @param configKey 配置键
     */
    void refreshCache(String configKey);
    
}

package com.edu.platform.report.service.impl;

import com.edu.platform.report.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    
    private static final String CACHE_PREFIX = "config:";
    private static final long CACHE_EXPIRE_HOURS = 24;
    
    @Override
    public String getConfigValue(String configKey) {
        // 1. 先从Redis缓存获取
        String cacheKey = CACHE_PREFIX + configKey;
        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedValue != null) {
            log.debug("从缓存获取配置: {}", configKey);
            return cachedValue;
        }
        
        // 2. 从数据库查询
        String sql = "SELECT config_value FROM sys_config WHERE config_key = ?";
        try {
            String value = jdbcTemplate.queryForObject(sql, String.class, configKey);
            
            // 3. 写入缓存
            if (value != null) {
                redisTemplate.opsForValue().set(cacheKey, value, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                log.info("配置已缓存: {}", configKey);
            }
            
            return value;
        } catch (Exception e) {
            log.error("查询配置失败: {}", configKey, e);
            return null;
        }
    }
    
    @Override
    public String getProfileWeights() {
        return getConfigValue("profile.weights");
    }
    
    @Override
    public void refreshCache(String configKey) {
        String cacheKey = CACHE_PREFIX + configKey;
        redisTemplate.delete(cacheKey);
        log.info("配置缓存已刷新: {}", configKey);
    }
    
}

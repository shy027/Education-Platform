package com.edu.platform.report.service.impl;

import com.edu.platform.report.dto.ConfigDTO;
import com.edu.platform.report.service.ConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(String configKey, String configValue) {
        String sql = "UPDATE sys_config SET config_value = ?, updated_time = NOW() WHERE config_key = ?";
        int rows = jdbcTemplate.update(sql, configValue, configKey);
        
        if (rows > 0) {
            // 刷新缓存
            refreshCache(configKey);
            log.info("配置更新成功: {} = {}", configKey, configValue);
        } else {
            log.warn("配置不存在,尝试插入: {}", configKey);
            // 如果不存在则插入
            String insertSql = "INSERT INTO sys_config (config_key, config_value, config_type, description, created_time, updated_time) " +
                             "VALUES (?, ?, 'CUSTOM', '自定义配置', NOW(), NOW())";
            jdbcTemplate.update(insertSql, configKey, configValue);
            log.info("配置插入成功: {} = {}", configKey, configValue);
        }
    }
    
    @Override
    public Map<String, BigDecimal> getDimensionWeights() {
        String weightsJson = getConfigValue("profile.dimension_weights");
        if (weightsJson == null) {
            // 返回默认权重 (6维)
            Map<String, BigDecimal> defaultWeights = new HashMap<>();
            defaultWeights.put("dimension_1", new BigDecimal("0.15"));
            defaultWeights.put("dimension_2", new BigDecimal("0.20"));
            defaultWeights.put("dimension_3", new BigDecimal("0.15"));
            defaultWeights.put("dimension_4", new BigDecimal("0.15"));
            defaultWeights.put("dimension_5", new BigDecimal("0.15"));
            defaultWeights.put("dimension_6", new BigDecimal("0.20"));
            return defaultWeights;
        }
        
        try {
            Map<String, Object> dimensionWeights = objectMapper.readValue(weightsJson, new TypeReference<Map<String, Object>>() {});
            Map<String, BigDecimal> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : dimensionWeights.entrySet()) {
                result.put(entry.getKey(), new BigDecimal(entry.getValue().toString()));
            }
            return result;
        } catch (Exception e) {
            log.error("解析维度权重配置失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, String> getDimensionNames() {
        String namesJson = getConfigValue("profile.dimension_names");
        if (namesJson == null) {
            Map<String, String> defaultNames = new HashMap<>();
            defaultNames.put("dimension1", "专业理论");
            defaultNames.put("dimension2", "技术技能");
            defaultNames.put("dimension3", "职业认同");
            defaultNames.put("dimension4", "工艺创新");
            defaultNames.put("dimension5", "社会责任");
            defaultNames.put("dimension6", "持续发展");
            return defaultNames;
        }
        try {
            return objectMapper.readValue(namesJson, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.error("解析维度名称配置失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public String getBehaviorWeights() {
        return getConfigValue("profile.behavior_weights");
    }
    
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDimensionWeights(Map<String, BigDecimal> weights) {
        try {
            Map<String, Object> dimensionWeights = new HashMap<>();
            for (Map.Entry<String, BigDecimal> entry : weights.entrySet()) {
                dimensionWeights.put(entry.getKey(), entry.getValue().doubleValue());
            }
            String updatedJson = objectMapper.writeValueAsString(dimensionWeights);
            updateConfig("profile.dimension_weights", updatedJson);
            log.info("维度权重更新成功: {}", dimensionWeights);
        } catch (Exception e) {
            log.error("更新维度权重失败", e);
            throw new RuntimeException("更新维度权重失败: " + e.getMessage());
        }
    }
    
    @Override
    public BigDecimal getExcellentThreshold() {
        try {
            String thresholdJson = getConfigValue("profile.level_thresholds");
            if (thresholdJson != null) {
                Map<String, Object> thresholds = objectMapper.readValue(thresholdJson, new TypeReference<Map<String, Object>>() {});
                if (thresholds.containsKey("excellent")) {
                    return new BigDecimal(thresholds.get("excellent").toString());
                }
            }
        } catch (Exception e) {
            log.warn("获取优秀阈值失败,使用默认值", e);
        }
        return new BigDecimal("90.00");
    }
    
    @Override
    public BigDecimal getGoodThreshold() {
        try {
            String thresholdJson = getConfigValue("profile.level_thresholds");
            if (thresholdJson != null) {
                Map<String, Object> thresholds = objectMapper.readValue(thresholdJson, new TypeReference<Map<String, Object>>() {});
                if (thresholds.containsKey("good")) {
                    return new BigDecimal(thresholds.get("good").toString());
                }
            }
        } catch (Exception e) {
            log.warn("获取良好阈值失败,使用默认值", e);
        }
        return new BigDecimal("80.00");
    }
    
    @Override
    public BigDecimal getPassThreshold() {
        try {
            String thresholdJson = getConfigValue("profile.level_thresholds");
            if (thresholdJson != null) {
                Map<String, Object> thresholds = objectMapper.readValue(thresholdJson, new TypeReference<Map<String, Object>>() {});
                if (thresholds.containsKey("pass")) {
                    return new BigDecimal(thresholds.get("pass").toString());
                }
            }
        } catch (Exception e) {
            log.warn("获取合格阈值失败,使用默认值", e);
        }
        return new BigDecimal("60.00");
    }
    
    @Override
    public Map<String, BigDecimal> getLevelThresholds() {
        try {
            String thresholdJson = getConfigValue("profile.level_thresholds");
            if (thresholdJson != null) {
                Map<String, Object> thresholds = objectMapper.readValue(thresholdJson, new TypeReference<Map<String, Object>>() {});
                Map<String, BigDecimal> result = new HashMap<>();
                for (Map.Entry<String, Object> entry : thresholds.entrySet()) {
                    result.put(entry.getKey(), new BigDecimal(entry.getValue().toString()));
                }
                return result;
            }
        } catch (Exception e) {
            log.warn("获取等级阈值配置失败,使用默认值", e);
        }
        
        Map<String, BigDecimal> defaultThresholds = new HashMap<>();
        defaultThresholds.put("excellent", new BigDecimal("90.00"));
        defaultThresholds.put("good", new BigDecimal("80.00"));
        defaultThresholds.put("pass", new BigDecimal("60.00"));
        return defaultThresholds;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLevelThresholds(Map<String, BigDecimal> thresholds) {
        try {
            Map<String, Object> levelThresholds = new HashMap<>();
            levelThresholds.put("excellent", thresholds.get("excellent").intValue());
            levelThresholds.put("good", thresholds.get("good").intValue());
            levelThresholds.put("pass", thresholds.get("pass").intValue());
            
            String updatedJson = objectMapper.writeValueAsString(levelThresholds);
            updateConfig("profile.level_thresholds", updatedJson);
            
            log.info("等级阈值更新成功: {}", levelThresholds);
        } catch (Exception e) {
            log.error("更新等级阈值失败", e);
            throw new RuntimeException("更新等级阈值失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<ConfigDTO> getAllConfigs() {
        String sql = "SELECT config_key, config_value, config_type, description FROM sys_config ORDER BY config_type, config_key";
        
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                ConfigDTO dto = new ConfigDTO();
                dto.setConfigKey(rs.getString("config_key"));
                dto.setConfigValue(rs.getString("config_value"));
                dto.setConfigType(rs.getString("config_type"));
                dto.setDescription(rs.getString("description"));
                return dto;
            });
        } catch (Exception e) {
            log.error("查询配置列表失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public void refreshCache(String configKey) {
        String cacheKey = CACHE_PREFIX + configKey;
        redisTemplate.delete(cacheKey);
        log.info("配置缓存已刷新: {}", configKey);
    }
}

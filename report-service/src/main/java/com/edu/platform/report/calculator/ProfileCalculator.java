package com.edu.platform.report.calculator;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.edu.platform.report.entity.BehaviorLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 素养画像计算器
 * 核心算法:根据学习行为计算五维度思政素养得分
 *
 * @author Education Platform
 */
@Slf4j
@Component
public class ProfileCalculator {
    
    /**
     * 计算五维度得分
     *
     * @param behaviorLogs 学习行为记录列表
     * @param weights 权重配置(JSON格式)
     * @return 五维度得分Map
     */
    public Map<String, BigDecimal> calculateDimensionScores(List<BehaviorLog> behaviorLogs, String weights) {
        Map<String, BigDecimal> scores = new HashMap<>();
        
        // 解析权重配置
        JSONObject weightConfig = JSONUtil.parseObj(weights);
        JSONObject behaviorWeights = weightConfig.getJSONObject("behaviorWeights");
        
        // 初始化五个维度得分
        scores.put("dimension1", BigDecimal.ZERO);  // 学习投入
        scores.put("dimension2", BigDecimal.ZERO);  // 知识掌握
        scores.put("dimension3", BigDecimal.ZERO);  // 思政参与
        scores.put("dimension4", BigDecimal.ZERO);  // 互动协作
        scores.put("dimension5", BigDecimal.ZERO);  // 反思成长
        
        // 按行为类型分组统计
        Map<String, Integer> behaviorCounts = new HashMap<>();
        Map<String, Integer> totalDurations = new HashMap<>();
        
        for (BehaviorLog log : behaviorLogs) {
            String behaviorType = log.getBehaviorType();
            behaviorCounts.merge(behaviorType, 1, Integer::sum);
            
            if (log.getDurationSeconds() != null) {
                totalDurations.merge(behaviorType, log.getDurationSeconds(), Integer::sum);
            }
        }
        
        // 计算各维度得分
        for (Map.Entry<String, Integer> entry : behaviorCounts.entrySet()) {
            String behaviorType = entry.getKey();
            int count = entry.getValue();
            
            // 获取该行为类型的权重配置
            JSONObject typeWeights = behaviorWeights.getJSONObject(behaviorType);
            if (typeWeights == null) {
                continue;
            }
            
            // 根据行为次数和权重计算各维度得分
            for (int i = 1; i <= 5; i++) {
                String dimension = "dimension_" + i;
                String dimensionKey = "dimension" + i;
                
                Double weight = typeWeights.getDouble(dimension);
                if (weight != null && weight > 0) {
                    // 得分 = 行为次数 × 权重 × 基础分
                    BigDecimal score = BigDecimal.valueOf(count)
                            .multiply(BigDecimal.valueOf(weight))
                            .multiply(BigDecimal.valueOf(10));  // 基础分10分
                    
                    scores.merge(dimensionKey, score, BigDecimal::add);
                }
            }
        }
        
        // 归一化处理:将得分限制在0-100之间
        for (String key : scores.keySet()) {
            BigDecimal score = scores.get(key);
            if (score.compareTo(BigDecimal.valueOf(100)) > 0) {
                scores.put(key, BigDecimal.valueOf(100));
            }
            // 保留2位小数
            scores.put(key, score.setScale(2, RoundingMode.HALF_UP));
        }
        
        return scores;
    }
    
    /**
     * 计算综合得分
     *
     * @param dimensionScores 五维度得分
     * @param weights 权重配置
     * @return 综合得分
     */
    public BigDecimal calculateTotalScore(Map<String, BigDecimal> dimensionScores, String weights) {
        JSONObject weightConfig = JSONUtil.parseObj(weights);
        JSONObject dimensionWeights = weightConfig.getJSONObject("dimensionWeights");
        
        BigDecimal totalScore = BigDecimal.ZERO;
        
        for (int i = 1; i <= 5; i++) {
            String dimensionKey = "dimension" + i;
            String weightKey = "dimension_" + i;
            
            BigDecimal score = dimensionScores.get(dimensionKey);
            Double weight = dimensionWeights.getDouble(weightKey);
            
            if (score != null && weight != null) {
                totalScore = totalScore.add(score.multiply(BigDecimal.valueOf(weight)));
            }
        }
        
        return totalScore.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 评定等级
     *
     * @param totalScore 综合得分
     * @param weights 权重配置(包含等级阈值)
     * @return 等级(优秀/良好/合格/待提升)
     */
    public String evaluateLevel(BigDecimal totalScore, String weights) {
        double score = totalScore.doubleValue();
        
        // 从配置中读取等级阈值
        JSONObject weightConfig = JSONUtil.parseObj(weights);
        JSONObject levelThresholds = weightConfig.getJSONObject("levelThresholds");
        
        if (levelThresholds != null) {
            // 使用配置的阈值
            double excellentThreshold = levelThresholds.getDouble("excellent", 90.0);
            double goodThreshold = levelThresholds.getDouble("good", 80.0);
            double passThreshold = levelThresholds.getDouble("pass", 60.0);
            
            if (score >= excellentThreshold) {
                return "优秀";
            } else if (score >= goodThreshold) {
                return "良好";
            } else if (score >= passThreshold) {
                return "合格";
            } else {
                return "待提升";
            }
        } else {
            // 使用默认阈值
            if (score >= 90) {
                return "优秀";
            } else if (score >= 80) {
                return "良好";
            } else if (score >= 60) {
                return "合格";
            } else {
                return "待提升";
            }
        }
    }
    
    /**
     * 判断成长趋势
     *
     * @param currentScore 当前得分
     * @param previousScore 上次得分
     * @return 趋势(上升/稳定/下降)
     */
    public String evaluateTrend(BigDecimal currentScore, BigDecimal previousScore) {
        if (previousScore == null) {
            return "稳定";
        }
        
        BigDecimal diff = currentScore.subtract(previousScore);
        
        if (diff.compareTo(BigDecimal.valueOf(5)) > 0) {
            return "上升";
        } else if (diff.compareTo(BigDecimal.valueOf(-5)) < 0) {
            return "下降";
        } else {
            return "稳定";
        }
    }
}

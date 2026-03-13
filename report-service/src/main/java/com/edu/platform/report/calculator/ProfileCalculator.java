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
     * 计算六维度得分 (双通道方案: 课程 80 + 资源 20)
     *
     * @param behaviorLogs 学习行为记录列表
     * @param resourceTagsMap 资源ID到标签列表的映射
     * @param behaviorWeightsJson 行为权重配置(JSON格式)
     * @param scoreConfig 评分分裂配置 (course_cap, resource_cap, resource_view_point)
     * @param tagWeightsConfig 资源标签权重与封顶配置
     * @return 六维度得分Map
     */
    public Map<String, BigDecimal> calculateDimensionScores(
            List<BehaviorLog> behaviorLogs,
            Map<Long, List<String>> resourceTagsMap,
            String behaviorWeightsJson,
            Map<String, BigDecimal> scoreConfig,
            Map<String, Object> tagWeightsConfig) {
        
        Map<String, BigDecimal> finalScores = new HashMap<>();
        Map<String, BigDecimal> courseScores = new HashMap<>();
        Map<String, BigDecimal> resourceScores = new HashMap<>();
        
        // 1. 初始化维度
        for (int i = 1; i <= 6; i++) {
            courseScores.put("dimension" + i, BigDecimal.ZERO);
            resourceScores.put("dimension" + i, BigDecimal.ZERO);
        }

        // 2. 分离行为类型
        List<BehaviorLog> courseLogs = new java.util.ArrayList<>();
        Map<String, Integer> tagCounts = new HashMap<>();
        
        java.util.Set<Long> viewedResourceIds = new java.util.HashSet<>();
        for (BehaviorLog log : behaviorLogs) {
            if ("RESOURCE_VIEW".equals(log.getBehaviorType())) {
                Long resId = log.getBehaviorObjectId();
                // 仅当资源ID不为空且该资源未被计分过时，累加标签分数
                if (resId != null && viewedResourceIds.add(resId)) {
                    List<String> tags = resourceTagsMap.get(resId);
                    if (tags != null) {
                        for (String tag : tags) {
                            tagCounts.merge(tag, 1, Integer::sum);
                        }
                    }
                }
            } else {
                courseLogs.add(log);
            }
        }

        // 3. 计算课程侧原始分 (截断至 course_cap)
        JSONObject behaviorWeights = JSONUtil.parseObj(behaviorWeightsJson);
        Map<String, Integer> courseBehaviorCounts = new HashMap<>();
        for (BehaviorLog log : courseLogs) {
            courseBehaviorCounts.merge(log.getBehaviorType(), 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : courseBehaviorCounts.entrySet()) {
            JSONObject weights = behaviorWeights.getJSONObject(entry.getKey());
            if (weights != null) {
                for (int i = 1; i <= 6; i++) {
                    String dimKey = "dimension" + i;
                    // 兼容带下划线和不带下划线的 key
                    Double w = weights.getDouble("dimension" + i);
                    if (w == null) {
                        w = weights.getDouble("dimension_" + i);
                    }
                    
                    if (w != null && w > 0) {
                        BigDecimal score = BigDecimal.valueOf(entry.getValue())
                                .multiply(BigDecimal.valueOf(w))
                                .multiply(BigDecimal.valueOf(10)); // 基础分10
                        courseScores.merge(dimKey, score, BigDecimal::add);
                    }
                }
            }
        }

        BigDecimal courseCap = scoreConfig.get("course_cap");
        if (courseCap == null) {
            throw new com.edu.platform.common.exception.BusinessException("核心配置缺失: profile.score_config.course_cap (课程得分上限)");
        }
        
        BigDecimal pointPerView = scoreConfig.get("resource_view_point");
        if (pointPerView == null) {
            throw new com.edu.platform.common.exception.BusinessException("核心配置缺失: profile.score_config.resource_view_point (单次浏览分值)");
        }

        for (String key : courseScores.keySet()) {
            BigDecimal score = courseScores.get(key);
            if (score.compareTo(courseCap) > 0) {
                courseScores.put(key, courseCap);
            }
        }

        // 4. 计算资源侧得分 (带标签级封顶, 截断至 resource_cap)
        for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
            String tagName = entry.getKey();
            int count = entry.getValue();
            
            Map<String, Object> tagConfig = (Map<String, Object>) tagWeightsConfig.get(tagName);
            if (tagConfig == null) continue;

            Object maxTagScoreObj = tagConfig.get("max_score");
            if (maxTagScoreObj == null) {
                // 如果标签上限缺失，则此标签无法计分，抛出异常提醒用户去前台完善
                throw new com.edu.platform.common.exception.BusinessException("配置缺失: 标签 [" + tagName + "] 未设置上限分 (max_score)");
            }
            BigDecimal maxTagScore = new BigDecimal(maxTagScoreObj.toString());
            
            Map<String, Object> weights = (Map<String, Object>) tagConfig.get("weights");
            if (weights == null) {
                throw new com.edu.platform.common.exception.BusinessException("配置缺失: 标签 [" + tagName + "] 未设置维度权重映射 (weights)");
            }

            log.debug("Processing tag: {}, count: {}, maxTagScore: {}", tagName, count, maxTagScore);
            
            // 标签基础分 = 次数 * 单价 (此处 pointPerView 已在前面校验过非空)
            BigDecimal baseBenefit = BigDecimal.valueOf(count).multiply(pointPerView);
            // 标签实得分 = min(基础分, 该标签上限)
            BigDecimal actualBenefit = baseBenefit.min(maxTagScore);

            for (int i = 1; i <= 6; i++) {
                String dimKey = "dimension" + i;
                Object wObj = weights.get(dimKey);
                // 增加带下划线的 key 兼容
                if (wObj == null) {
                    wObj = weights.get("dimension_" + i);
                }
                
                if (wObj != null) {
                    BigDecimal weightStar = new BigDecimal(wObj.toString());
                    // 贡献 = 实得分 * (星级 / 5.0)
                    BigDecimal contrib = actualBenefit.multiply(weightStar).divide(new BigDecimal("5.0"), 4, RoundingMode.HALF_UP);
                    resourceScores.merge(dimKey, contrib, BigDecimal::add);
                }
            }
        }

        BigDecimal resourceCap = scoreConfig.getOrDefault("resource_cap", new BigDecimal("20.0"));
        for (String key : resourceScores.keySet()) {
            BigDecimal score = resourceScores.get(key);
            if (score.compareTo(resourceCap) > 0) {
                resourceScores.put(key, resourceCap);
            }
        }

        // 5. 合并得分并保留2位小数
        for (int i = 1; i <= 6; i++) {
            String dimKey = "dimension" + i;
            BigDecimal total = courseScores.get(dimKey).add(resourceScores.get(dimKey));
            if (total.compareTo(new BigDecimal("100")) > 0) {
                total = new BigDecimal("100");
            }
            finalScores.put(dimKey, total.setScale(2, RoundingMode.HALF_UP));
        }

        return finalScores;
    }
    
    /**
     * 计算综合得分
     *
     * @param dimensionScores 六维度得分
     * @param dimensionWeights 维度权重配置
     * @return 综合得分
     */
    public BigDecimal calculateTotalScore(Map<String, BigDecimal> dimensionScores, Map<String, BigDecimal> dimensionWeights) {
        BigDecimal totalScore = BigDecimal.ZERO;
        
        for (int i = 1; i <= 6; i++) {
            String dimensionKey = "dimension" + i;
            
            BigDecimal score = dimensionScores.get(dimensionKey);
            BigDecimal weight = dimensionWeights.get(dimensionKey);
            if (weight == null) {
                weight = dimensionWeights.get("dimension_" + i);
            }
            
            if (score != null && weight != null) {
                totalScore = totalScore.add(score.multiply(weight));
            }
        }
        
        return totalScore.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 评定等级
     *
     * @param totalScore 综合得分
     * @param thresholds 等级阈值配置Map
     * @return 等级(优秀/良好/合格/待提升)
     */
    public String evaluateLevel(BigDecimal totalScore, Map<String, BigDecimal> thresholds) {
        double score = totalScore.doubleValue();
        
        double excellentThreshold = thresholds.getOrDefault("excellent", BigDecimal.valueOf(90.0)).doubleValue();
        double goodThreshold = thresholds.getOrDefault("good", BigDecimal.valueOf(80.0)).doubleValue();
        double passThreshold = thresholds.getOrDefault("pass", BigDecimal.valueOf(60.0)).doubleValue();
        
        if (score >= excellentThreshold) {
            return "优秀";
        } else if (score >= goodThreshold) {
            return "良好";
        } else if (score >= passThreshold) {
            return "合格";
        } else {
            return "待提升";
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

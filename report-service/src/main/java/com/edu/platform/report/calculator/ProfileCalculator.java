package com.edu.platform.report.calculator;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.edu.platform.common.dto.CourseScoringDTO;
import com.edu.platform.common.result.Result;
import com.edu.platform.report.client.CourseClient;
import com.edu.platform.report.entity.BehaviorLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 素养画像计算器
 * 核心算法: 根据学习行为计算基于课程权重和表现比例的素养得分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileCalculator {

    private final CourseClient courseClient;

    /**
     * 计算六维度得分 (双通道方案: 课程占比 80% + 资源占比 20%)
     *
     * @param behaviorLogs        学习行为记录列表
     * @param resourceTagsMap     资源ID到标签列表的映射
     * @param behaviorWeightsJson 全局默认行为权重 (兜底)
     * @param scoreConfig         评分构成配置 (course_cap, resource_cap, resource_view_point)
     * @param tagWeightsConfig    资源标签权重配置
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
        
        // 用于缓存课程配置，避免重复调用
        Map<Long, CourseScoringDTO> courseCache = new HashMap<>();

        // 1. 初始化维度和分值 (改为 5 维度)
        for (int i = 1; i <= 5; i++) {
            courseScores.put("dimension" + i, BigDecimal.ZERO);
            resourceScores.put("dimension" + i, BigDecimal.ZERO);
        }

        // 全局默认行为权重 (兜底用)
        JSONObject defaultBehaviorWeights = JSONUtil.parseObj(behaviorWeightsJson);

        // 2. 遍历行为日志
        // 去重处理资源浏览：同一资源只计分一次
        java.util.Set<Long> viewedResourceIds = new java.util.HashSet<>();
        java.util.Set<Long> viewedCoursewareIds = new java.util.HashSet<>();
        java.util.Set<Long> viewedTopicIds = new java.util.HashSet<>();
        java.util.Map<Long, BigDecimal> taskMaxPoints = new java.util.HashMap<>();
        java.util.Map<Long, CourseScoringDTO> taskConfigs = new java.util.HashMap<>();

        for (BehaviorLog logEntry : behaviorLogs) {
            String type = logEntry.getBehaviorType();
            Long courseId = logEntry.getCourseId();

            // A. 处理资源浏览记录 (仅限全局资源库浏览，其 courseId 通常为 0 或空)
            if (isViewBehavior(type) && (courseId == null || courseId == 0)) {
                Long resId = logEntry.getBehaviorObjectId();
                if (resId != null && viewedResourceIds.add(resId)) {
                    calculateResourceContribution(logEntry, resourceTagsMap, tagWeightsConfig, scoreConfig, resourceScores);
                }
                continue;
            }

            // B. 处理课程关联行为 (非浏览行为，如做题、讨论)
            if (courseId == null || courseId <= 0) {
                continue;
            }

            // 获取或缓存课程评分元数据
            CourseScoringDTO cScoring = courseCache.computeIfAbsent(courseId, id -> {
                try {
                    Result<CourseScoringDTO> result = courseClient.getCourseDetail(id);
                    return (result != null && result.getCode() == 200) ? result.getData() : null;
                } catch (Exception e) {
                    log.error("获取课程评分配置失败, courseId={}", id, e);
                    return null;
                }
            });

            if (cScoring == null) {
                // 不再进行兜底，直接跳过
                continue;
            }

            Long objId = logEntry.getBehaviorObjectId();

            // 1. 课件计分去重 (同一个课件只计分一次)
            if (isViewBehavior(type)) {
                if (objId != null && !viewedCoursewareIds.add(objId)) {
                    continue;
                }
            }

            // 2. 话题计分去重 (同一个话题回复多次仅计一次，包含回复的回复)
            if ("POST_COMMENT".equals(type) || "GROUP_DISCUSS".equals(type)) {
                Long topicId = objId;
                // 对于 POST_COMMENT，需要区分是发帖还是回复，并统一使用 postId 去重
                if ("POST_COMMENT".equals(type) && logEntry.getBehaviorData() != null) {
                    try {
                        JSONObject data = JSONUtil.parseObj(logEntry.getBehaviorData());
                        // 如果是回复 (isPost=false)，objId 是评论 ID，真正的去重 ID 是 postId
                        if (Boolean.FALSE.equals(data.getBool("isPost"))) {
                            topicId = data.getLong("postId");
                        }
                    } catch (Exception e) {
                        // 忽略解析错误，退回到 objId
                    }
                }
                if (topicId != null && !viewedTopicIds.add(topicId)) {
                    continue;
                }
            }

            BigDecimal points = calculateBehaviorPoints(logEntry, defaultBehaviorWeights);
            
            // 3. 任务计分优化 (同一个任务取历次提交的最高分)
            if ("SUBMIT_TASK".equals(type) || "SUBMIT_HOMEWORK".equals(type) || "SUBMIT_ANSWER".equals(type)) {
                if (objId != null) {
                    BigDecimal currentMax = taskMaxPoints.getOrDefault(objId, BigDecimal.ZERO);
                    if (points.compareTo(currentMax) > 0) {
                        taskMaxPoints.put(objId, points);
                        taskConfigs.put(objId, cScoring);
                    }
                }
                continue;
            }

            if (points.compareTo(BigDecimal.ZERO) <= 0) {
                // 在没有基础分数的情况下，若是浏览类行为，尝试根据资源标签补分 (计入 resourceScores)
                if (isViewBehavior(type)) {
                    calculateResourceContribution(logEntry, resourceTagsMap, tagWeightsConfig, scoreConfig, resourceScores);
                }
                continue;
            }

            // 根据该课程选中的维度全额分摊点数
            distributeFullToSelectedDimensions(points, cScoring, courseScores);
        }

        // 4. 将各任务的最高得分批量结算
        for (java.util.Map.Entry<Long, BigDecimal> entry : taskMaxPoints.entrySet()) {
            distributeFullToSelectedDimensions(entry.getValue(), taskConfigs.get(entry.getKey()), courseScores);
        }

        // 3. 封顶处理并合并结果
        BigDecimal courseCap = scoreConfig.getOrDefault("course_cap", new BigDecimal("80"));
        BigDecimal resourceCap = scoreConfig.getOrDefault("resource_cap", new BigDecimal("20"));

        for (int i = 1; i <= 5; i++) {
            String dimKey = "dimension" + i;
            
            // 课程侧得分上限限制
            BigDecimal cScore = courseScores.get(dimKey).setScale(2, RoundingMode.HALF_UP);
            if (cScore.compareTo(courseCap) > 0) {
                cScore = courseCap;
            }

            // 资源侧得分上限限制
            BigDecimal rScore = resourceScores.get(dimKey).setScale(2, RoundingMode.HALF_UP);
            if (rScore.compareTo(resourceCap) > 0) {
                rScore = resourceCap;
            }

            BigDecimal total = cScore.add(rScore);
            if (total.compareTo(new BigDecimal("100")) > 0) {
                total = new BigDecimal("100");
            }
            finalScores.put(dimKey, total.setScale(2, RoundingMode.HALF_UP));
        }

        return finalScores;
    }

    /**
     * 计算单次行为产生的点数 (基于全局默认权重)
     */
    private BigDecimal calculateBehaviorPoints(BehaviorLog logEntry, JSONObject defaultWeights) {
        String type = logEntry.getBehaviorType();
        
        // 1. 类型别名兼容
        if ("SUBMIT_HOMEWORK".equals(type)) type = "SUBMIT_TASK";
        if ("SUBMIT_ANSWER".equals(type)) type = "SUBMIT_TASK";
        if ("WATCH_VIDEO".equals(type) || "READ_DOC".equals(type)) type = "VIEW_COURSEWARE"; // 统一按课件浏览计分

        // 2. 获取该行为的基础分值
        BigDecimal basePoints = BigDecimal.ZERO;
        Object val = defaultWeights.get(type);
        if (val instanceof Number) {
            basePoints = new BigDecimal(val.toString());
        } else if (val instanceof JSONObject) {
            basePoints = ((JSONObject) val).getBigDecimal("points", BigDecimal.ZERO);
        }

        if (basePoints == null || basePoints.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 3. 如果是提交任务类型，且带有表现分数数据，则按比例计算
        if ("SUBMIT_TASK".equals(type) && logEntry.getBehaviorData() != null) {
            try {
                JSONObject data = JSONUtil.parseObj(logEntry.getBehaviorData());
                BigDecimal userScore = data.getBigDecimal("score", null);
                BigDecimal totalScore = data.getBigDecimal("total", null);

                if (userScore != null && totalScore != null && totalScore.compareTo(BigDecimal.ZERO) > 0) {
                    return userScore.divide(totalScore, 4, RoundingMode.HALF_UP).multiply(basePoints);
                }
            } catch (Exception e) {
                log.warn("解析任务表现数据失败, behaviorId={}", logEntry.getId());
            }
        }

        // 4. 话题得分过滤：如果是发布话题（Teacher 行为），则不计分
        if ("POST_COMMENT".equals(type) && logEntry.getBehaviorData() != null) {
            try {
                JSONObject data = JSONUtil.parseObj(logEntry.getBehaviorData());
                if (Boolean.TRUE.equals(data.getBool("isPost"))) {
                    return BigDecimal.ZERO;
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        return basePoints;
    }

    /**
     * 全额分配：将点数全额叠加到所有选中的维度轴上 (不进行均分)
     */
    private void distributeFullToSelectedDimensions(BigDecimal points, CourseScoringDTO config, Map<String, BigDecimal> scores) {
        String weightsStr = config.getDimensionWeights();
        if (weightsStr == null || weightsStr.trim().isEmpty() || "{}".equals(weightsStr)) {
            // 不再进行兜底，直接返回
            return;
        }
        
        JSONObject dimWeights = JSONUtil.parseObj(weightsStr);
        if (dimWeights == null) {
            return;
        }

        for (int i = 1; i <= 5; i++) {
            String key = "dimension" + i;
            BigDecimal weight = dimWeights.getBigDecimal(key);
            if (weight == null) weight = dimWeights.getBigDecimal("dimension_" + i, BigDecimal.ZERO);
            
            // 兼容性逻辑：无论是旧版的百分比权重(0.x)还是新版的勾选(1.0)
            // 只要权重 > 0，就视为该行为影响此维度，按照“不分摊”原则全额叠加
            if (weight.compareTo(BigDecimal.ZERO) > 0) {
                scores.put(key, scores.get(key).add(points));
            }
        }
    }

    private boolean isViewBehavior(String type) {
        return "RESOURCE_VIEW".equals(type) || "WATCH_VIDEO".equals(type) || 
               "READ_DOC".equals(type) || "VIEW_CASE".equals(type) || 
               "VIEW_COURSEWARE".equals(type);
    }

    /**
     * 计算资源浏览对维度的贡献
     */
    private void calculateResourceContribution(
            BehaviorLog logEntry,
            Map<Long, List<String>> resourceTagsMap,
            Map<String, Object> tagWeightsConfig,
            Map<String, BigDecimal> scoreConfig,
            Map<String, BigDecimal> resourceScores) {

        List<String> tags = resourceTagsMap.get(logEntry.getBehaviorObjectId());
        if (tags == null || tags.isEmpty()) {
            return;
        }

        BigDecimal viewPoint = scoreConfig.getOrDefault("resource_view_point", new BigDecimal("0.1"));

        for (String tag : tags) {
            Object tagConfig = tagWeightsConfig.get(tag);
            if (tagConfig instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) tagConfig;
                
                // 兼容旧结构 or 直接在 config 里的权重
                Map<String, Object> weightsMap;
                if (config.containsKey("weights") && config.get("weights") instanceof Map) {
                    weightsMap = (Map<String, Object>) config.get("weights");
                } else {
                    weightsMap = config;
                }

                // 标签本身可能也有上限分限制 (max_score)
                // TODO: 考虑在全局画像处理器中实现跨行为的标签封顶逻辑

                for (int i = 1; i <= 5; i++) {
                    String dimKey = "dimension" + i;
                    Object val = weightsMap.get(dimKey);
                    if (val == null) {
                        val = weightsMap.get("dimension_" + i);
                    }
                    
                    if (val != null) {
                        BigDecimal weightVal = new BigDecimal(val.toString());
                        // 换算逻辑：如果是星级评分 (1.0 - 5.0)，则除以 5 得到 0.2 - 1.0 的实际权重系数
                        // 如果原本存的就是小数 (0.x)，只要 > 1 就在此做转换，兼容新旧方案
                        BigDecimal weight = weightVal.compareTo(BigDecimal.ONE) > 0 
                                ? weightVal.divide(new BigDecimal("5.0"), 4, RoundingMode.HALF_UP) 
                                : weightVal;
                        
                        // 贡献 = 单次浏览分 * 标签在此维度的占比权重
                        BigDecimal contribution = viewPoint.multiply(weight);
                        
                        // 注意：这里的 maxTagScore 是针对整个标签的，但我们现在是按记录遍历
                        // 逻辑上应该是： baseBenefit = count * viewPoint; actualBenefit = min(baseBenefit, maxTagScore)
                        // 但因为 calculateDimensionScores 是平铺遍历记录的，且 resourceScores 在不断累加
                        // 所以这里的 maxTagScore 应该在 calculateDimensionScores 的最外层对每个标签做统计处理。
                        // 为了简化并保持兼容，我们暂时允许累加，但在最终 resourceScores 时做 resourceCap 封顶。
                        resourceScores.put(dimKey, resourceScores.get(dimKey).add(contribution));
                    }
                }
            }
        }
    }

    /**
     * 计算综合得分
     */
    public BigDecimal calculateTotalScore(Map<String, BigDecimal> dimensionScores, Map<String, BigDecimal> dimensionWeights) {
        BigDecimal totalScore = BigDecimal.ZERO;
        for (int i = 1; i <= 5; i++) {
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
     */
    public String evaluateLevel(BigDecimal totalScore, Map<String, BigDecimal> thresholds) {
        double score = totalScore.doubleValue();
        double excellentThreshold = thresholds.getOrDefault("excellent", new BigDecimal("90.0")).doubleValue();
        double goodThreshold = thresholds.getOrDefault("good", new BigDecimal("80.0")).doubleValue();
        double passThreshold = thresholds.getOrDefault("pass", new BigDecimal("60.0")).doubleValue();
        
        if (score >= excellentThreshold) return "优秀";
        if (score >= goodThreshold) return "良好";
        if (score >= passThreshold) return "合格";
        return "待提升";
    }

    /**
     * 判断成长趋势
     */
    public String evaluateTrend(BigDecimal currentScore, BigDecimal previousScore) {
        if (previousScore == null) return "稳定";
        BigDecimal diff = currentScore.subtract(previousScore);
        if (diff.compareTo(BigDecimal.valueOf(5)) > 0) return "上升";
        if (diff.compareTo(BigDecimal.valueOf(-5)) < 0) return "下降";
        return "稳定";
    }
}

package com.edu.platform.report.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.report.dto.BehaviorWeightsUpdateRequest;
import com.edu.platform.report.dto.ConfigDTO;
import com.edu.platform.report.dto.ScoreConfigUpdateRequest;
import com.edu.platform.report.dto.TagWeightsUpdateRequest;
import com.edu.platform.report.dto.ThresholdsUpdateRequest;
import com.edu.platform.report.dto.WeightsUpdateRequest;
import com.edu.platform.report.service.ConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员配置控制器
 *
 * @author Education Platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/config")
@RequiredArgsConstructor
@Tag(name = "管理员配置", description = "系统配置管理接口(仅管理员)")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminConfigController {
    
    private final ConfigService configService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 获取权重配置
     */
    @GetMapping("/weights")
    @Operation(summary = "获取权重配置", description = "获取五维度权重配置")
    public Result<Map<String, BigDecimal>> getWeights() {
        try {
            Map<String, BigDecimal> weights = configService.getDimensionWeights();
            return Result.success(weights);
        } catch (Exception e) {
            log.error("获取权重配置失败", e);
            return Result.fail("获取权重配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取维度名称配置
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_SCHOOL_LEADER')")
    @GetMapping("/dimension-names")
    @Operation(summary = "获取维度名称", description = "获取维度Key与展示名称的映射")
    public Result<Map<String, String>> getDimensionNames() {
        try {
            return Result.success(configService.getDimensionNames());
        } catch (Exception e) {
            log.error("获取维度名称失败", e);
            return Result.fail("获取维度名称失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新权重配置
     */
    @PutMapping("/weights")
    @Operation(summary = "更新权重配置", description = "更新五维度权重配置,总和必须为1.0")
    public Result<Void> updateWeights(@RequestBody WeightsUpdateRequest request) {
        try {
            // 转换为Map
            Map<String, BigDecimal> weights = new HashMap<>();
            weights.put("dimension1", request.getDimension1());
            weights.put("dimension2", request.getDimension2());
            weights.put("dimension3", request.getDimension3());
            weights.put("dimension4", request.getDimension4());
            weights.put("dimension5", request.getDimension5());
            
            // 验证权重总和是否为1
            BigDecimal sum = weights.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (sum.compareTo(BigDecimal.ONE) != 0) {
                return Result.fail("权重总和必须为1.0,当前总和为: " + sum);
            }
            
            configService.updateDimensionWeights(weights);
            return Result.success();
        } catch (Exception e) {
            log.error("更新权重配置失败", e);
            return Result.fail("更新权重配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取等级阈值配置
     */
    @GetMapping("/thresholds")
    @Operation(summary = "获取等级阈值", description = "获取优秀/良好/合格的分数阈值")
    public Result<Map<String, BigDecimal>> getThresholds() {
        try {
            Map<String, BigDecimal> thresholds = new HashMap<>();
            thresholds.put("excellent", configService.getExcellentThreshold());
            thresholds.put("good", configService.getGoodThreshold());
            thresholds.put("pass", configService.getPassThreshold());
            return Result.success(thresholds);
        } catch (Exception e) {
            log.error("获取阈值配置失败", e);
            return Result.fail("获取阈值配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新等级阈值配置
     */
    @PutMapping("/thresholds")
    @Operation(summary = "更新等级阈值", description = "更新优秀/良好/合格的分数阈值,必须满足: excellent > good > pass > 0")
    public Result<Void> updateThresholds(@RequestBody ThresholdsUpdateRequest request) {
        try {
            // 验证阈值的合理性: excellent > good > pass > 0
            BigDecimal excellent = request.getExcellent();
            BigDecimal good = request.getGood();
            BigDecimal pass = request.getPass();
            
            if (excellent == null || good == null || pass == null) {
                return Result.fail("必须提供所有阈值(excellent, good, pass)");
            }
            
            if (excellent.compareTo(good) <= 0 || good.compareTo(pass) <= 0 || 
                pass.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.fail("阈值必须满足: excellent > good > pass > 0");
            }
            
            // 转换为Map
            Map<String, BigDecimal> thresholds = new HashMap<>();
            thresholds.put("excellent", excellent);
            thresholds.put("good", good);
            thresholds.put("pass", pass);
            
            // 更新profile.weights中的levelThresholds
            configService.updateLevelThresholds(thresholds);
            
            return Result.success();
        } catch (Exception e) {
            log.error("更新阈值配置失败", e);
            return Result.fail("更新阈值配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取行为基础分值配置
     */
    @GetMapping("/behavior-weights")
    @Operation(summary = "获取行为基础分值", description = "获取回复讨论/任务提交等行为的基础加分值")
    public Result<Map<String, BigDecimal>> getBehaviorWeights() {
        try {
            String json = configService.getBehaviorWeights();
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
            Map<String, BigDecimal> weights = new HashMap<>();
            
            // 定义我们关心的行为 Key
            String[] keys = {"VIEW_COURSEWARE", "SUBMIT_TASK", "POST_COMMENT", "ESSENCE_POST"};
            for (String key : keys) {
                com.fasterxml.jackson.databind.JsonNode node = root.get(key);
                if (node == null || node.isNull()) {
                    weights.put(key, BigDecimal.ZERO);
                } else if (node.isNumber()) {
                    weights.put(key, node.decimalValue());
                } else {
                    // 核心修复逻辑：如果是旧版的嵌套对象格式，这里不再报错，而是将其视为 0
                    // 这样前端页面能正常打开，用户修改并保存后，数据库中的旧格式会被新格式（纯数值）覆盖
                    log.info("检测到非数值格式行为权重 (可能为旧版嵌套数据), key={}, value={}", key, node);
                    weights.put(key, BigDecimal.ZERO);
                }
            }
            return Result.success(weights);
        } catch (Exception e) {
            log.error("获取行为权重失败", e);
            Map<String, BigDecimal> defaults = new HashMap<>();
            defaults.put("VIEW_COURSEWARE", BigDecimal.ZERO);
            defaults.put("SUBMIT_TASK", BigDecimal.ZERO);
            defaults.put("POST_COMMENT", BigDecimal.ZERO);
            defaults.put("ESSENCE_POST", BigDecimal.ZERO);
            return Result.success(defaults);
        }
    }

    /**
     * 更新行为基础分值配置
     */
    @PutMapping("/behavior-weights")
    @Operation(summary = "更新行为基础分值", description = "更新回复讨论/任务提交等行为的基础加分值")
    public Result<Void> updateBehaviorWeights(@RequestBody BehaviorWeightsUpdateRequest request) {
        try {
            Map<String, BigDecimal> weights = new HashMap<>();
            weights.put("VIEW_COURSEWARE", request.getViewCourseware());
            weights.put("SUBMIT_TASK", request.getSubmitTask());
            weights.put("POST_COMMENT", request.getPostReply());
            weights.put("ESSENCE_POST", request.getEssencePost());
            
            configService.updateBehaviorWeights(weights);
            return Result.success();
        } catch (Exception e) {
            log.error("更新行为权重失败", e);
            return Result.fail("更新行为权重失败: " + e.getMessage());
        }
    }

    /**
     * 获取评分占比及上限配置
     */
    @GetMapping("/score-config")
    @Operation(summary = "获取评分占比及上限配置", description = "获取课程/资源侧分值上限及单次浏览得分")
    public Result<Map<String, BigDecimal>> getScoreConfig() {
        try {
            return Result.success(configService.getScoreConfig());
        } catch (Exception e) {
            log.error("获取评分配置失败", e);
            return Result.fail("获取评分配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新评分占比及上限配置
     */
    @PutMapping("/score-config")
    @Operation(summary = "更新评分占比及上限配置", description = "更新课程/资源侧分值上限及单次浏览得分")
    public Result<Void> updateScoreConfig(@RequestBody ScoreConfigUpdateRequest request) {
        try {
            Map<String, BigDecimal> config = new HashMap<>();
            config.put("course_cap", request.getCourseCap());
            config.put("resource_cap", request.getResourceCap());
            config.put("resource_view_point", request.getResourceViewPoint());
            
            configService.updateScoreConfig(config);
            return Result.success();
        } catch (Exception e) {
            log.error("更新评分配置失败", e);
            return Result.fail("更新评分配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取资源标签权重配置
     */
    @GetMapping("/tag-weights")
    @Operation(summary = "获取资源标签权重", description = "获取资源标签与各维度的分摊权重")
    public Result<Map<String, Object>> getTagWeights() {
        try {
            return Result.success(configService.getResourceTagWeights());
        } catch (Exception e) {
            log.error("获取标签权重失败", e);
            return Result.fail("获取标签权重失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新资源标签权重配置
     */
    @PutMapping("/tag-weights")
    @Operation(summary = "更新资源标签权重", description = "批量更新资源标签与各维度的分摊权重")
    public Result<Void> updateTagWeights(@RequestBody TagWeightsUpdateRequest request) {
        try {
            // 将 DTO 转回简单的 Map<String, Object> 存储
            Map<String, Object> data = new HashMap<>();
            if (request.getTagConfigs() != null) {
                for (Map.Entry<String, TagWeightsUpdateRequest.TagConfig> entry : request.getTagConfigs().entrySet()) {
                    Map<String, Object> innerMap = new HashMap<>();
                    innerMap.put("max_score", entry.getValue().getMaxScore());
                    innerMap.put("weights", entry.getValue().getWeights());
                    data.put(entry.getKey(), innerMap);
                }
            }
            
            configService.updateResourceTagWeights(data);
            return Result.success();
        } catch (Exception e) {
            log.error("更新标签权重失败", e);
            return Result.fail("更新标签权重失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取测试题型默认分值配置
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_SCHOOL_LEADER')")
    @GetMapping("/exam-scores")
    @Operation(summary = "获取测试题型默认分值", description = "获取各题型的默认分值配置")
    public Result<Map<String, Integer>> getExamDefaultScores() {
        try {
            String json = configService.getConfigValue("exam.default_scores");
            if (json == null) {
                // 返给前端兜底默认值
                Map<String, Integer> defaultScores = new HashMap<>();
                defaultScores.put("1", 5);
                defaultScores.put("2", 5);
                defaultScores.put("3", 5);
                defaultScores.put("4", 5);
                defaultScores.put("5", 10);
                defaultScores.put("6", 10);
                return Result.success(defaultScores);
            }
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
            Map<String, Integer> scores = new HashMap<>();
            root.fields().forEachRemaining(entry -> {
                scores.put(entry.getKey(), entry.getValue().asInt());
            });
            return Result.success(scores);
        } catch (Exception e) {
            log.error("获取题型默认分值失败", e);
            return Result.fail("获取题型默认分值失败: " + e.getMessage());
        }
    }

    /**
     * 更新测试题型默认分值配置
     */
    @PutMapping("/exam-scores")
    @Operation(summary = "更新测试题型默认分值", description = "仅管理员可更新测试题型默认分值")
    public Result<Void> updateExamDefaultScores(@RequestBody Map<String, Integer> scores) {
        try {
            String json = objectMapper.writeValueAsString(scores);
            configService.updateConfig("exam.default_scores", json);
            return Result.success();
        } catch (Exception e) {
            log.error("更新题型默认分值失败", e);
            return Result.fail("更新题型默认分值失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有配置
     */
    @GetMapping
    @Operation(summary = "获取所有配置", description = "获取系统所有配置项")
    public Result<List<ConfigDTO>> getAllConfigs() {
        try {
            List<ConfigDTO> configs = configService.getAllConfigs();
            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取配置列表失败", e);
            return Result.fail("获取配置列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 刷新配置缓存
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新配置缓存", description = "刷新指定配置项的缓存")
    public Result<Void> refreshCache(@RequestParam String configKey) {
        try {
            configService.refreshCache(configKey);
            return Result.success();
        } catch (Exception e) {
            log.error("刷新配置缓存失败", e);
            return Result.fail("刷新配置缓存失败: " + e.getMessage());
        }
    }
}

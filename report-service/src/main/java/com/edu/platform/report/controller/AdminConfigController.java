package com.edu.platform.report.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.report.dto.ConfigDTO;
import com.edu.platform.report.dto.ThresholdsUpdateRequest;
import com.edu.platform.report.dto.WeightsUpdateRequest;
import com.edu.platform.report.service.ConfigService;
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
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfigController {
    
    private final ConfigService configService;
    
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
     * 更新权重配置
     */
    @PutMapping("/weights")
    @Operation(summary = "更新权重配置", description = "更新五维度权重配置,总和必须为1.0")
    public Result<Void> updateWeights(@RequestBody WeightsUpdateRequest request) {
        try {
            // 转换为Map
            Map<String, BigDecimal> weights = new HashMap<>();
            weights.put("dimension_1", request.getDimension_1());
            weights.put("dimension_2", request.getDimension_2());
            weights.put("dimension_3", request.getDimension_3());
            weights.put("dimension_4", request.getDimension_4());
            weights.put("dimension_5", request.getDimension_5());
            
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

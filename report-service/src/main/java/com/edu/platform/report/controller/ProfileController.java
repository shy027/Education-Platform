package com.edu.platform.report.controller;

import cn.hutool.core.bean.BeanUtil;
import com.edu.platform.common.result.Result;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.report.dto.response.ProfileResponse;
import com.edu.platform.report.service.ProfileService;
import com.edu.platform.report.task.ProfileScheduledTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 素养画像控制器
 *
 * @author Education Platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "素养画像管理", description = "素养画像查询和计算接口")
public class ProfileController {
    
    private final ProfileService profileService;
    private final ProfileScheduledTask profileScheduledTask;
    
    /**
     * 获取当前用户的素养画像
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的素养画像", description = "获取当前登录用户在指定课程的素养画像")
    public Result<ProfileResponse> getMyProfile(
            @Parameter(description = "课程ID", required = true)
            @RequestParam Long courseId) {
        
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.fail("未登录");
        }
        
        Map<String, Object> profileData = profileService.getProfile(userId, courseId);
        
        if (!(Boolean) profileData.get("exists")) {
            return Result.fail("暂无素养画像数据,请先进行学习");
        }
        
        ProfileResponse response = BeanUtil.toBean(profileData, ProfileResponse.class);
        return Result.success(response);
    }
    
    /**
     * 获取指定用户的素养画像(教师/管理员)
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户素养画像", description = "获取指定用户在指定课程的素养画像")
    public Result<ProfileResponse> getUserProfile(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "课程ID", required = true)
            @RequestParam Long courseId) {
        
        Map<String, Object> profileData = profileService.getProfile(userId, courseId);
        
        if (!(Boolean) profileData.get("exists")) {
            return Result.fail("该用户暂无素养画像数据");
        }
        
        ProfileResponse response = BeanUtil.toBean(profileData, ProfileResponse.class);
        return Result.success(response);
    }
    
    /**
     * 手动触发画像计算(管理员)
     */
    @PostMapping("/calculate")
    @Operation(summary = "手动触发画像计算", description = "手动触发指定课程的素养画像计算")
    public Result<Void> calculateProfiles(
            @Parameter(description = "课程ID", required = true)
            @RequestParam Long courseId) {
        
        try {
            profileScheduledTask.triggerCalculation(courseId);
            return Result.success();
        } catch (Exception e) {
            log.error("触发画像计算失败", e);
            return Result.fail("触发画像计算失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算单个用户的画像
     */
    @PostMapping("/calculate/user")
    @Operation(summary = "计算单个用户画像", description = "计算指定用户在指定课程的素养画像")
    public Result<Void> calculateUserProfile(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "课程ID", required = true)
            @RequestParam Long courseId) {
        
        try {
            profileService.calculateProfile(userId, courseId);
            return Result.success();
        } catch (Exception e) {
            log.error("计算用户画像失败", e);
            return Result.fail("计算画像失败: " + e.getMessage());
        }
    }
    
}

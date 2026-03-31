package com.edu.platform.report.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.Result;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.report.dto.response.GrowthTrackResponse;
import com.edu.platform.report.dto.response.ProfileResponse;
import com.edu.platform.report.dto.response.RadarDataResponse;
import com.edu.platform.report.dto.response.StatisticsResponse;
import com.edu.platform.report.entity.StudentProfile;
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
     * 分页获取学生画像列表(管理员/教师)
     */
    @GetMapping
    @Operation(summary = "分页获取学生画像列表", description = "分页查询所有学生的素养画像")
    public Result<IPage<StudentProfile>> listProfiles(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "学校ID") @RequestParam(required = false) Long schoolId,
            @Parameter(description = "院系/部门") @RequestParam(required = false) String department,
            @Parameter(description = "班级") @RequestParam(required = false) String className) {
        // 权限校验
        if (!UserContext.hasRole("ADMIN") && !UserContext.hasRole("SCHOOL_LEADER") && !UserContext.hasRole("TEACHER")) {
            return Result.fail("无权查询学生画像列表");
        }
        
        Page<StudentProfile> page = new Page<>(current, size);
        IPage<StudentProfile> result = profileService.listProfiles(page, courseId, schoolId, department, className);
        return Result.success(result);
    }
    
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
    
    @Operation(summary = "获取雷达图数据", description = "获取用户在指定课程的五维雷达图数据")
    @GetMapping("/radar")
    public Result<RadarDataResponse> getRadarData(
            @Parameter(description = "课程ID", required = true) @RequestParam Long courseId,
            @Parameter(description = "用户ID (管理员/教师可用)") @RequestParam(required = false) Long userId) {
        
        Long targetUserId = userId;
        if (targetUserId == null) {
            targetUserId = UserContext.getUserId();
        } else {
            // 权限校验
            if (!UserContext.hasRole("ADMIN") && !UserContext.hasRole("SCHOOL_LEADER") && !UserContext.hasRole("TEACHER")) {
                return Result.fail("无权查询其他用户的画像数据");
            }
        }

        if (targetUserId == null) {
            return Result.fail("未登录");
        }
        
        RadarDataResponse response = profileService.getRadarData(targetUserId, courseId);
        if (response == null) {
            return Result.fail("暂无素养画像数据");
        }
        
        return Result.success(response);
    }
    
    @Operation(summary = "获取成长轨迹", description = "获取用户在指定课程的成长轨迹数据")
    @GetMapping("/growth-track")
    public Result<GrowthTrackResponse> getGrowthTrack(
            @Parameter(description = "课程ID", required = true) @RequestParam Long courseId,
            @Parameter(description = "用户ID (管理员/教师可用)") @RequestParam(required = false) Long userId,
            @Parameter(description = "天数", required = false) @RequestParam(defaultValue = "30") Integer days) {
        
        Long targetUserId = userId;
        if (targetUserId == null) {
            targetUserId = UserContext.getUserId();
        } else {
            if (!UserContext.hasRole("ADMIN") && !UserContext.hasRole("SCHOOL_LEADER") && !UserContext.hasRole("TEACHER")) {
                return Result.fail("无权查询其他用户的画像数据");
            }
        }

        if (targetUserId == null) {
            return Result.fail("未登录");
        }
        
        GrowthTrackResponse response = profileService.getGrowthTrack(targetUserId, courseId, days);
        return Result.success(response);
    }
    
    @Operation(summary = "获取学习统计", description = "获取用户在指定课程的学习统计数据")
    @GetMapping("/statistics")
    public Result<StatisticsResponse> getStatistics(
            @Parameter(description = "课程ID", required = true) @RequestParam Long courseId,
            @Parameter(description = "用户ID (管理员/教师可用)") @RequestParam(required = false) Long userId,
            @Parameter(description = "天数", required = false) @RequestParam(defaultValue = "30") Integer days) {
        
        Long targetUserId = userId;
        if (targetUserId == null) {
            targetUserId = UserContext.getUserId();
        } else {
            if (!UserContext.hasRole("ADMIN") && !UserContext.hasRole("SCHOOL_LEADER") && !UserContext.hasRole("TEACHER")) {
                return Result.fail("无权查询其他用户的画像数据");
            }
        }

        if (targetUserId == null) {
            return Result.fail("未登录");
        }
        
        StatisticsResponse response = profileService.getStatistics(targetUserId, courseId, days);
        return Result.success(response);
    }
    
}

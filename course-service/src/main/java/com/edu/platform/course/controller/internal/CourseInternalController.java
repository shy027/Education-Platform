package com.edu.platform.course.controller.internal;

import com.edu.platform.common.result.Result;
import com.edu.platform.common.dto.CourseScoringDTO;
import com.edu.platform.course.entity.Course;
import com.edu.platform.course.service.CourseService;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import lombok.Data;

/**
 * 课程内部接口控制器
 */
@Tag(name = "课程内部接口")
@RestController
@RequestMapping("/internal/course")
@RequiredArgsConstructor
public class CourseInternalController {

    private final CourseService courseService;

    @Operation(summary = "获取课程详情(内部调用)")
    @GetMapping("/{id}")
    public Result<CourseScoringDTO> getCourseDetail(@PathVariable Long id) {
        Course course = courseService.getById(id);
        if (course == null) {
            return Result.fail("课程不存在");
        }
        CourseScoringDTO dto = new CourseScoringDTO();
        BeanUtil.copyProperties(course, dto);
        return Result.success(dto);
    }

    /**
     * 更新课程审核状态 (由audit-service调用)
     */
    @Operation(summary = "更新课程审核状态")
    @PutMapping("/{courseId}/audit-status")
    public Result<Void> updateAuditStatus(
            @PathVariable Long courseId,
            @RequestBody UpdateAuditStatusRequest request) {
        courseService.updateCourseAuditStatus(courseId, request.getAuditStatus(),
                request.getAuditorId(), request.getAuditRemark());
        return Result.success();
    }

    /**
     * 获取课程基本信息 (用于审核中心展示)
     */
    @Operation(summary = "获取课程基本信息")
    @GetMapping("/{courseId}/info")
    public Result<java.util.Map<String, Object>> getCourseInfo(@PathVariable Long courseId) {
        return Result.success(courseService.getCourseInfo(courseId));
    }

    /**
     * 获取课程统计信息 (内部调用)
     */
    @Operation(summary = "获取课程统计信息")
    @GetMapping("/stats")
    public Result<java.util.Map<String, Object>> getCourseStats() {
        return Result.success(courseService.getCourseStats());
    }

    @Data
    public static class UpdateAuditStatusRequest {
        private Integer auditStatus;
        private Long auditorId;
        private String auditRemark;
    }
}

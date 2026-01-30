package com.edu.platform.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.annotation.RequireAdminOrLeader;
import com.edu.platform.common.annotation.RequireTeacherOrAbove;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.request.CoursewareQueryRequest;
import com.edu.platform.course.dto.request.CoursewareUpdateRequest;
import com.edu.platform.course.dto.request.CoursewareUploadRequest;
import com.edu.platform.course.dto.request.ProgressRecordRequest;
import com.edu.platform.course.dto.response.CoursewareDetailResponse;
import com.edu.platform.course.dto.response.CoursewareResponse;
import com.edu.platform.course.entity.CoursewareProgress;
import com.edu.platform.course.service.CoursewareProgressService;
import com.edu.platform.course.service.CoursewareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课件管理控制器
 *
 * @author Education Platform
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "课件管理", description = "课件上传、查询、审核、学习进度等接口")
public class CoursewareController {
    
    private final CoursewareService coursewareService;
    private final CoursewareProgressService progressService;
    
    @PostMapping("/courses/{courseId}/coursewares")
    @RequireTeacherOrAbove
    @Operation(summary = "上传课件", description = "教师上传课件到指定课程")
    public Result<Long> uploadCourseware(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Valid @RequestBody CoursewareUploadRequest request) {
        Long userId = UserContext.getUserId();
        Long wareId = coursewareService.uploadCourseware(courseId, request, userId);
        return Result.success(wareId);
    }
    
    @PutMapping("/coursewares/{wareId}")
    @Operation(summary = "更新课件", description = "更新课件信息")
    public Result<Void> updateCourseware(
            @Parameter(description = "课件ID") @PathVariable Long wareId,
            @Valid @RequestBody CoursewareUpdateRequest request) {
        request.setId(wareId);
        Long userId = UserContext.getUserId();
        coursewareService.updateCourseware(request, userId);
        return Result.success();
    }
    
    @DeleteMapping("/coursewares/{wareId}")
    @Operation(summary = "删除课件", description = "删除课件(软删除)")
    public Result<Void> deleteCourseware(
            @Parameter(description = "课件ID") @PathVariable Long wareId) {
        Long userId = UserContext.getUserId();
        coursewareService.deleteCourseware(wareId, userId);
        return Result.success();
    }
    
    @GetMapping("/courses/{courseId}/coursewares")
    @Operation(summary = "获取课件列表", description = "分页查询课程的课件列表,支持按章节和类型筛选")
    public Result<Page<CoursewareResponse>> getCoursewareList(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "章节ID") @RequestParam(required = false) Long chapterId,
            @Parameter(description = "课件类型: 1-视频 2-文档 3-PPT 4-音频") @RequestParam(required = false) Integer wareType,
            @Parameter(description = "审核状态: 0-待审核 1-通过 2-拒绝") @RequestParam(required = false) Integer auditStatus,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        CoursewareQueryRequest request = new CoursewareQueryRequest();
        request.setChapterId(chapterId);
        request.setWareType(wareType);
        request.setAuditStatus(auditStatus);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        Page<CoursewareResponse> page = coursewareService.getCoursewareList(courseId, request);
        return Result.success(page);
    }
    
    @GetMapping("/coursewares/{wareId}")
    @Operation(summary = "获取课件详情", description = "获取课件详细信息,包含学习进度")
    public Result<CoursewareDetailResponse> getCoursewareDetail(
            @Parameter(description = "课件ID") @PathVariable Long wareId) {
        Long userId = UserContext.getUserId();
        CoursewareDetailResponse detail = coursewareService.getCoursewareDetail(wareId, userId);
        return Result.success(detail);
    }
    
    @PostMapping("/coursewares/{wareId}/audit")
    @RequireAdminOrLeader
    @Operation(summary = "审核课件", description = "管理员或校领导审核课件")
    public Result<Void> auditCourseware(
            @Parameter(description = "课件ID") @PathVariable Long wareId,
            @Parameter(description = "审核状态: 1-通过 2-不通过") @RequestParam Integer auditStatus) {
        Long auditorId = UserContext.getUserId();
        coursewareService.auditCourseware(wareId, auditStatus, auditorId);
        return Result.success();
    }
    
    @PostMapping("/coursewares/{wareId}/progress")
    @Operation(summary = "记录学习进度", description = "学生记录课件学习进度")
    public Result<Void> recordProgress(
            @Parameter(description = "课件ID") @PathVariable Long wareId,
            @Valid @RequestBody ProgressRecordRequest request) {
        Long userId = UserContext.getUserId();
        progressService.recordProgress(wareId, request, userId);
        return Result.success();
    }
    
    @GetMapping("/coursewares/{wareId}/progress")
    @Operation(summary = "获取学习进度", description = "获取当前用户的课件学习进度")
    public Result<CoursewareProgress> getProgress(
            @Parameter(description = "课件ID") @PathVariable Long wareId) {
        Long userId = UserContext.getUserId();
        CoursewareProgress progress = progressService.getProgress(wareId, userId);
        return Result.success(progress);
    }
    
    @GetMapping("/coursewares/{wareId}/progress/students")
    @RequireTeacherOrAbove
    @Operation(summary = "获取课件学习进度统计", description = "教师查看该课件所有学生的学习进度")
    public Result<Page<com.edu.platform.course.dto.response.StudentProgressResponse>> getCoursewareProgress(
            @Parameter(description = "课件ID") @PathVariable Long wareId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<com.edu.platform.course.dto.response.StudentProgressResponse> page = 
                progressService.getCoursewareProgress(wareId, pageNum, pageSize);
        return Result.success(page);
    }
    
    @GetMapping("/courses/{courseId}/progress/student/{userId}")
    @RequireTeacherOrAbove
    @Operation(summary = "获取学生课程学习进度", description = "教师查看指定学生在该课程下所有课件的学习进度")
    public Result<Page<com.edu.platform.course.dto.response.StudentProgressResponse>> getCourseProgress(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "学生ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<com.edu.platform.course.dto.response.StudentProgressResponse> page = 
                progressService.getCourseProgress(courseId, userId, pageNum, pageSize);
        return Result.success(page);
    }
}

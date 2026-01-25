package com.edu.platform.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.request.CourseAuditRequest;
import com.edu.platform.course.dto.request.CourseQueryRequest;
import com.edu.platform.course.dto.response.CourseListResponse;
import com.edu.platform.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员课程管理接口
 */
@Tag(name = "管理员-课程管理")
@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseService courseService;

    @Operation(summary = "获取待审核课程列表")
    @GetMapping("/pending")
    public Result<Page<CourseListResponse>> getPendingCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String subjectArea,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        CourseQueryRequest request = new CourseQueryRequest();
        request.setKeyword(keyword);
        request.setSchoolId(schoolId);
        request.setSubjectArea(subjectArea);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        return Result.success(courseService.getPendingCourses(request));
    }

    @Operation(summary = "审核课程")
    @PutMapping("/{id}/audit")
    public Result<Void> auditCourse(@PathVariable Long id,
                                     @RequestBody @Validated CourseAuditRequest request) {
        courseService.auditCourse(id, request);
        return Result.success("审核完成", null);
    }
}

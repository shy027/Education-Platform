package com.edu.platform.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.request.CourseCreateRequest;
import com.edu.platform.course.dto.request.CourseQueryRequest;
import com.edu.platform.course.dto.request.CourseUpdateRequest;
import com.edu.platform.course.dto.response.CourseDetailResponse;
import com.edu.platform.course.dto.response.CourseListResponse;
import com.edu.platform.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 课程管理接口
 */
@Tag(name = "课程管理")
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "创建课程")
    @PostMapping
    public Result<Long> createCourse(@RequestBody @Validated CourseCreateRequest request) {
        Long courseId = courseService.createCourse(request);
        return Result.success("课程创建成功", courseId);
    }

    @Operation(summary = "更新课程")
    @PutMapping
    public Result<Void> updateCourse(@RequestBody @Validated CourseUpdateRequest request) {
        courseService.updateCourse(request);
        return Result.success("课程更新成功", null);
    }

    @Operation(summary = "获取课程详情")
    @GetMapping("/{id}")
    public Result<CourseDetailResponse> getCourseDetail(@PathVariable Long id) {
        return Result.success(courseService.getCourseDetail(id));
    }

    @Operation(summary = "分页查询课程列表")
    @GetMapping
    public Result<Page<CourseListResponse>> pageCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String subjectArea,
            @RequestParam(required = false) Integer joinType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        CourseQueryRequest request = new CourseQueryRequest();
        request.setKeyword(keyword);
        request.setSchoolId(schoolId);
        request.setSubjectArea(subjectArea);
        request.setJoinType(joinType);
        request.setStatus(status);
        request.setAuditStatus(auditStatus);
        request.setTeacherId(teacherId);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        return Result.success(courseService.pageCourses(request));
    }

    @Operation(summary = "修改课程状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        courseService.updateStatus(id, status);
        return Result.success();
    }
}

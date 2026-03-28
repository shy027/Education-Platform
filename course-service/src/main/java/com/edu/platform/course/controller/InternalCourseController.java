package com.edu.platform.course.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 内部调用统计接口
 */
@Tag(name = "内部接口-课程统计")
@RestController
@RequestMapping("/internal/course")
@RequiredArgsConstructor
public class InternalCourseController {

    private final CourseService courseService;

    @Operation(summary = "获取课程看板统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getCourseStats() {
        return Result.success(courseService.getCourseStats());
    }
}

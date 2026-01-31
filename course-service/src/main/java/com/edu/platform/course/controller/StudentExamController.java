package com.edu.platform.course.controller;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.response.ExamListResponse;
import com.edu.platform.course.dto.response.PaperResponse;
import com.edu.platform.course.service.StudentExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学生考试控制器
 */
@Tag(name = "学生考试")
@RestController
@RequestMapping("/api/v1/student/exams")
@RequiredArgsConstructor
public class StudentExamController {

    private final StudentExamService studentExamService;

    @Operation(summary = "获取考试列表")
    @GetMapping
    public Result<PageResult<ExamListResponse>> getStudentExams(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageResult<ExamListResponse> result = studentExamService.getStudentExams(courseId, status, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取考试详情")
    @GetMapping("/{taskId}")
    public Result<PaperResponse> getExamDetail(@PathVariable Long taskId) {
        PaperResponse response = studentExamService.getExamDetail(taskId);
        return Result.success(response);
    }

    @Operation(summary = "开始考试")
    @PostMapping("/{taskId}/start")
    public Result<Long> startExam(@PathVariable Long taskId) {
        Long recordId = studentExamService.startExam(taskId);
        return Result.success("考试已开始", recordId);
    }
}

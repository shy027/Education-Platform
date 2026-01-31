package com.edu.platform.course.controller;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.request.GradeAnswerRequest;
import com.edu.platform.course.dto.response.GradingResultResponse;
import com.edu.platform.course.service.GradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 批改控制器
 */
@Tag(name = "教师批改")
@RestController
@RequestMapping("/api/v1/grading")
@RequiredArgsConstructor
public class GradingController {

    private final GradingService gradingService;

    @Operation(summary = "获取待批改列表")
    @GetMapping("/pending")
    public Result<PageResult<GradingResultResponse>> getPendingGrading(
            @RequestParam Long taskId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageResult<GradingResultResponse> result = gradingService.getPendingGrading(taskId, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "批改单题")
    @PostMapping("/grade")
    public Result<Void> gradeAnswer(@RequestBody GradeAnswerRequest request) {
        gradingService.gradeAnswer(request);
        return Result.success();
    }

    @Operation(summary = "发布成绩")
    @PostMapping("/{recordId}/publish")
    public Result<Void> publishGrade(@PathVariable Long recordId) {
        gradingService.publishGrade(recordId);
        return Result.success();
    }

    @Operation(summary = "获取批改结果")
    @GetMapping("/{recordId}")
    public Result<GradingResultResponse> getGradingResult(@PathVariable Long recordId) {
        GradingResultResponse response = gradingService.getGradingResult(recordId);
        return Result.success(response);
    }
}

package com.edu.platform.course.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.request.SaveAnswerRequest;
import com.edu.platform.course.dto.response.AnswerProgressResponse;
import com.edu.platform.course.service.AnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 答题控制器
 */
@Tag(name = "学生答题")
@RestController
@RequestMapping("/api/v1/student/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @Operation(summary = "保存答案")
    @PostMapping("/save")
    public Result<Void> saveAnswer(@RequestBody SaveAnswerRequest request) {
        answerService.saveAnswer(request);
        return Result.success();
    }

    @Operation(summary = "提交答卷")
    @PostMapping("/{recordId}/submit")
    public Result<Void> submitExam(@PathVariable Long recordId) {
        answerService.submitExam(recordId);
        return Result.success();
    }

    @Operation(summary = "获取答题进度")
    @GetMapping("/{recordId}")
    public Result<AnswerProgressResponse> getAnswerProgress(@PathVariable Long recordId) {
        AnswerProgressResponse response = answerService.getAnswerProgress(recordId);
        return Result.success(response);
    }
}

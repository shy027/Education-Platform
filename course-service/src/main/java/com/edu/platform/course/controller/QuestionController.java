package com.edu.platform.course.controller;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.request.QuestionCreateRequest;
import com.edu.platform.course.dto.request.QuestionQueryRequest;
import com.edu.platform.course.dto.request.QuestionUpdateRequest;
import com.edu.platform.course.dto.response.QuestionResponse;
import com.edu.platform.course.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 题目管理控制器
 */
@Tag(name = "题目管理")
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "创建题目")
    @PostMapping
    public Result<Long> createQuestion(@RequestBody QuestionCreateRequest request) {
        Long questionId = questionService.createQuestion(request);
        return Result.success(questionId);
    }

    @Operation(summary = "更新题目")
    @PutMapping("/{questionId}")
    public Result<Void> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody QuestionUpdateRequest request) {
        questionService.updateQuestion(questionId, request);
        return Result.success();
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/{questionId}")
    public Result<Void> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return Result.success();
    }

    @Operation(summary = "获取题目详情")
    @GetMapping("/{questionId}")
    public Result<QuestionResponse> getQuestionDetail(@PathVariable Long questionId) {
        QuestionResponse response = questionService.getQuestionDetail(questionId);
        return Result.success(response);
    }

    @Operation(summary = "查询题目列表")
    @GetMapping
    public Result<PageResult<QuestionResponse>> listQuestions(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        QuestionQueryRequest request = new QuestionQueryRequest();
        request.setCourseId(courseId);
        request.setChapterId(chapterId);
        request.setQuestionType(questionType);
        request.setDifficulty(difficulty);
        request.setKeyword(keyword);
        request.setCreatorId(creatorId);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        PageResult<QuestionResponse> result = questionService.listQuestions(request);
        return Result.success(result);
    }
}

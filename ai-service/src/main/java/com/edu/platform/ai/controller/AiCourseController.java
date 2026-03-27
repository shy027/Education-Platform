package com.edu.platform.ai.controller;

import com.edu.platform.ai.dto.response.AiCourseAnalysisResponse;
import com.edu.platform.ai.dto.response.AiRecommendationResponse;
import com.edu.platform.ai.service.AiService;
import com.edu.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI 课程功能控制器
 */
@Tag(name = "AI 课程管理")
@RestController
@RequestMapping("/api/v1/ai/course")
@RequiredArgsConstructor
public class AiCourseController {

    private final AiService aiService;

    @Operation(summary = "分析课程文档获取建议信息")
    @PostMapping("/analyze")
    public Result<AiCourseAnalysisResponse> analyzeCourse(@RequestParam("file") MultipartFile file) {
        AiCourseAnalysisResponse response = aiService.analyzeCourseDocument(file);
        return Result.success(response);
    }

    @Operation(summary = "获取 AI 资源推荐")
    @GetMapping("/{courseId}/recommend-resources")
    public Result<AiRecommendationResponse> recommendResources(
            @PathVariable Long courseId,
            @RequestParam(required = false) Long chapterId) {
        AiRecommendationResponse response = aiService.recommendResources(courseId, chapterId);
        return Result.success(response);
    }

    @Operation(summary = "通过分析课程文档获取 AI 资源推荐")
    @PostMapping("/recommend-resources/analyze-file")
    public Result<AiRecommendationResponse> recommendResourcesByFile(@RequestParam("file") MultipartFile file) {
        AiRecommendationResponse response = aiService.recommendResourcesByDocument(file);
        return Result.success(response);
    }
}

package com.edu.platform.course.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.request.ChapterCreateRequest;
import com.edu.platform.course.dto.request.ChapterUpdateRequest;
import com.edu.platform.course.dto.response.ChapterTreeResponse;
import com.edu.platform.course.service.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 章节管理接口
 */
@Tag(name = "章节管理")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    @Operation(summary = "获取章节树")
    @GetMapping("/tree")
    public Result<List<ChapterTreeResponse>> getChapterTree(@PathVariable Long courseId) {
        return Result.success(chapterService.getChapterTree(courseId));
    }

    @Operation(summary = "创建章节")
    @PostMapping
    public Result<Long> createChapter(@PathVariable Long courseId, @RequestBody @Validated ChapterCreateRequest request) {
        request.setCourseId(courseId);
        Long chapterId = chapterService.createChapter(request);
        return Result.success("章节创建成功", chapterId);
    }

    @Operation(summary = "更新章节")
    @PutMapping
    public Result<Void> updateChapter(@PathVariable Long courseId, @RequestBody @Validated ChapterUpdateRequest request) {
        chapterService.updateChapter(request);
        return Result.success("章节更新成功", null);
    }

    @Operation(summary = "删除章节")
    @DeleteMapping("/{id}")
    public Result<Void> deleteChapter(@PathVariable Long courseId, @PathVariable Long id) {
        chapterService.deleteChapter(id);
        return Result.success("章节删除成功", null);
    }
}

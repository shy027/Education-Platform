package com.edu.platform.course.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.course.service.ChapterResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 章节资源管理接口
 */
@Tag(name = "章节资源关联管理")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/chapters/{chapterId}/resources")
@RequiredArgsConstructor
public class ChapterResourceController {

    private final ChapterResourceService chapterResourceService;

    @Operation(summary = "绑定资源到章节")
    @PostMapping("/{resourceId}")
    public Result<Void> bindResource(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long resourceId) {
        chapterResourceService.bindResource(courseId, chapterId, resourceId);
        return Result.success("绑定成功", null);
    }

    @Operation(summary = "取消绑定资源")
    @DeleteMapping("/{resourceId}")
    public Result<Void> unbindResource(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long resourceId) {
        chapterResourceService.unbindResource(courseId, chapterId, resourceId);
        return Result.success("取消绑定成功", null);
    }
}

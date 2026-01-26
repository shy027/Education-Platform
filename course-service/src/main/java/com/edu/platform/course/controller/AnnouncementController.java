package com.edu.platform.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.request.AnnouncementCreateRequest;
import com.edu.platform.course.dto.request.AnnouncementQueryRequest;
import com.edu.platform.course.dto.request.AnnouncementUpdateRequest;
import com.edu.platform.course.dto.response.AnnouncementResponse;
import com.edu.platform.course.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公告管理接口
 */
@Tag(name = "公告管理")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "发布公告")
    @PostMapping
    public Result<Long> createAnnouncement(@PathVariable Long courseId,
                                            @RequestBody @Validated AnnouncementCreateRequest request) {
        request.setCourseId(courseId);
        Long id = announcementService.createAnnouncement(request);
        return Result.success("发布成功", id);
    }

    @Operation(summary = "更新公告")
    @PutMapping
    public Result<Void> updateAnnouncement(@PathVariable Long courseId,
                                            @RequestBody @Validated AnnouncementUpdateRequest request) {
        announcementService.updateAnnouncement(request);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long courseId,
                                            @PathVariable Long id) {
        announcementService.deleteAnnouncement(courseId, id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取公告详情")
    @GetMapping("/{id}")
    public Result<AnnouncementResponse> getAnnouncementDetail(@PathVariable Long courseId,
                                                                @PathVariable Long id) {
        return Result.success(announcementService.getAnnouncementDetail(courseId, id));
    }

    @Operation(summary = "分页查询公告列表")
    @GetMapping
    public Result<Page<AnnouncementResponse>> pageAnnouncements(
            @PathVariable Long courseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isTop,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        AnnouncementQueryRequest request = new AnnouncementQueryRequest();
        request.setCourseId(courseId);
        request.setKeyword(keyword);
        request.setIsTop(isTop);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        return Result.success(announcementService.pageAnnouncements(request));
    }

    @Operation(summary = "置顶/取消置顶公告")
    @PutMapping("/{id}/top")
    public Result<Void> toggleTop(@PathVariable Long courseId,
                                   @PathVariable Long id,
                                   @RequestParam Integer isTop) {
        announcementService.toggleTop(courseId, id, isTop);
        return Result.success("操作成功", null);
    }
}

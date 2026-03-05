package com.edu.platform.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.request.SubjectCategoryRequest;
import com.edu.platform.course.dto.response.SubjectCategoryResponse;
import com.edu.platform.course.service.SubjectCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学科领域分类管理接口
 */
@Tag(name = "学科领域分类管理")
@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
public class SubjectCategoryController {

    private final SubjectCategoryService subjectCategoryService;

    @Operation(summary = "获取所有已启用的学科分类", description = "用于前端下拉框展示")
    @GetMapping
    public Result<List<SubjectCategoryResponse>> getAllEnabledSubjects() {
        return Result.success(subjectCategoryService.getAllEnabledSubjects());
    }

    @Operation(summary = "分页获取学科分类列表", description = "管理员用")
    @GetMapping("/admin")
    public Result<Page<SubjectCategoryResponse>> pageSubjects(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(subjectCategoryService.pageSubjects(pageNum, pageSize, keyword));
    }

    @Operation(summary = "新增学科分类", description = "管理员用")
    @PostMapping
    public Result<Void> createSubject(@RequestBody @Validated SubjectCategoryRequest request) {
        subjectCategoryService.createSubject(request);
        return Result.success();
    }

    @Operation(summary = "更新学科分类", description = "管理员用")
    @PutMapping("/{id}")
    public Result<Void> updateSubject(@PathVariable Long id, @RequestBody @Validated SubjectCategoryRequest request) {
        subjectCategoryService.updateSubject(id, request);
        return Result.success();
    }

    @Operation(summary = "删除学科分类", description = "管理员用")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSubject(@PathVariable Long id) {
        subjectCategoryService.deleteSubject(id);
        return Result.success();
    }

    @Operation(summary = "切换启用状态", description = "管理员用")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer isEnabled) {
        subjectCategoryService.updateStatus(id, isEnabled);
        return Result.success();
    }
}

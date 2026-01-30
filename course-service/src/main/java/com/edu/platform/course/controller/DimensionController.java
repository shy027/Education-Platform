package com.edu.platform.course.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.course.annotation.RequireAdmin;
import com.edu.platform.course.dto.request.DimensionCreateRequest;
import com.edu.platform.course.dto.response.DimensionResponse;
import com.edu.platform.course.service.DimensionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 能力维度管理控制器
 * 维度由管理员统一创建和管理,教师在创建题目时从已有维度中选择
 */
@Tag(name = "能力维度管理")
@RestController
@RequestMapping("/api/v1/dimensions")
@RequiredArgsConstructor
public class DimensionController {

    private final DimensionService dimensionService;

    @Operation(summary = "创建能力维度", description = "仅管理员可操作")
    @RequireAdmin
    @PostMapping
    public Result<Long> createDimension(@RequestBody DimensionCreateRequest request) {
        Long dimensionId = dimensionService.createDimension(request);
        return Result.success(dimensionId);
    }

    @Operation(summary = "查询所有能力维度", description = "所有用户可查询,用于题目关联维度时选择")
    @GetMapping
    public Result<List<DimensionResponse>> listDimensions() {
        List<DimensionResponse> responses = dimensionService.listDimensions();
        return Result.success(responses);
    }

    @Operation(summary = "删除能力维度", description = "仅管理员可操作")
    @RequireAdmin
    @DeleteMapping("/{dimensionId}")
    public Result<Void> deleteDimension(@PathVariable Long dimensionId) {
        dimensionService.deleteDimension(dimensionId);
        return Result.success();
    }

    @Operation(summary = "更新能力维度", description = "仅管理员可操作")
    @RequireAdmin
    @PutMapping("/{dimensionId}")
    public Result<Void> updateDimension(@PathVariable Long dimensionId, 
                                        @RequestBody DimensionCreateRequest request) {
        dimensionService.updateDimension(dimensionId, request);
        return Result.success();
    }
}

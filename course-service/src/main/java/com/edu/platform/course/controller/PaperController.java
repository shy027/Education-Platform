package com.edu.platform.course.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.request.ManualPaperRequest;
import com.edu.platform.course.dto.request.RandomPaperRequest;
import com.edu.platform.course.dto.response.PaperResponse;
import com.edu.platform.course.service.PaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 试卷管理控制器
 */
@Tag(name = "试卷管理")
@RestController
@RequestMapping("/api/v1/papers")
@RequiredArgsConstructor
public class PaperController {

    private final PaperService paperService;

    @Operation(summary = "手动组卷")
    @PostMapping("/manual")
    public Result<Void> assembleManualPaper(@RequestBody ManualPaperRequest request) {
        paperService.assembleManualPaper(request);
        return Result.success();
    }

    @Operation(summary = "随机组卷")
    @PostMapping("/random")
    public Result<Void> assembleRandomPaper(@RequestBody RandomPaperRequest request) {
        paperService.assembleRandomPaper(request);
        return Result.success();
    }

    @Operation(summary = "获取试卷详情")
    @GetMapping("/{taskId}")
    public Result<PaperResponse> getPaperDetail(@PathVariable Long taskId) {
        PaperResponse response = paperService.getPaperDetail(taskId);
        return Result.success(response);
    }

    @Operation(summary = "删除试卷")
    @DeleteMapping("/{taskId}")
    public Result<Void> deletePaper(@PathVariable Long taskId) {
        paperService.deletePaper(taskId);
        return Result.success();
    }
}

package com.edu.platform.resource.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 内部调用统计接口
 */
@Tag(name = "内部接口-资源统计")
@RestController
@RequestMapping("/internal/resource")
@RequiredArgsConstructor
public class InternalResourceController {

    private final ResourceService resourceService;

    @Operation(summary = "获取资源看板统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getResourceStats() {
        return Result.success(resourceService.getResourceStats());
    }
}

package com.edu.platform.report.controller;

import com.edu.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查Controller
 *
 * @author Education Platform
 */
@Tag(name = "健康检查", description = "系统健康检查接口")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    
    @Operation(summary = "健康检查")
    @GetMapping
    public Result<String> health() {
        return Result.success("Report Service is running!");
    }
    
}

package com.edu.platform.report.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.report.dto.request.BehaviorLogRequest;
import com.edu.platform.report.service.BehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 行为埋点Controller
 *
 * @author Education Platform
 */
@Tag(name = "行为埋点", description = "学习行为埋点接口")
@RestController
@RequestMapping("/api/v1/behaviors")
@RequiredArgsConstructor
public class BehaviorController {
    
    private final BehaviorService behaviorService;
    
    @Operation(summary = "记录学习行为")
    @PostMapping("/log")
    public Result<Void> logBehavior(@RequestBody BehaviorLogRequest request) {
        behaviorService.logBehavior(request);
        return Result.success();
    }
    
}

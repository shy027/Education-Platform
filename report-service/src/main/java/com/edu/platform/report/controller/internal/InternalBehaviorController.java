package com.edu.platform.report.controller.internal;

import com.edu.platform.common.result.Result;
import com.edu.platform.common.dto.BehaviorLogDTO;
import com.edu.platform.report.dto.request.BehaviorLogRequest;
import com.edu.platform.report.service.BehaviorService;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 行为日志内部接口控制器
 */
@Tag(name = "行为日志内部接口")
@RestController
@RequestMapping("/internal/behavior")
@RequiredArgsConstructor
public class InternalBehaviorController {

    private final BehaviorService behaviorService;

    @Operation(summary = "记录行为日志(内部调用)")
    @PostMapping("/log")
    public Result<Void> logBehavior(@RequestBody BehaviorLogDTO dto) {
        BehaviorLogRequest request = new BehaviorLogRequest();
        BeanUtil.copyProperties(dto, request);
        behaviorService.logBehavior(request);
        return Result.success();
    }
}

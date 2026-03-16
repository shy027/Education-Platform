package com.edu.platform.community.client;

import com.edu.platform.common.dto.BehaviorLogDTO;
import com.edu.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 行为日志上报接口客户端 (调用report-service)
 */
@FeignClient(name = "report-service", path = "/internal/behavior")
public interface BehaviorClient {

    /**
     * 上报行为日志
     */
    @PostMapping("/log")
    Result<Void> logBehavior(@RequestBody BehaviorLogDTO request);
}

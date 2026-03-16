package com.edu.platform.course.client;

import com.edu.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 行为日志上报接口客户端 (调用report-service)
 */
@FeignClient(name = "report-service", path = "/internal/behavior")
public interface BehaviorClient {

    @PostMapping("/log")
    Result<Void> logBehavior(@RequestBody com.edu.platform.common.dto.BehaviorLogDTO request);

    @org.springframework.web.bind.annotation.DeleteMapping("/log")
    Result<Void> deleteBehavior(
            @org.springframework.web.bind.annotation.RequestParam("type") String type,
            @org.springframework.web.bind.annotation.RequestParam("objectId") Long objectId);
}

package com.edu.platform.resource.controller.internal;

import com.edu.platform.common.result.Result;
import com.edu.platform.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 资源内部接口控制器(供其他服务调用)
 *
 * @author Education Platform
 */
@Tag(name = "资源内部接口")
@RestController
@RequestMapping("/internal/resource")
@RequiredArgsConstructor
public class ResourceInternalController {

    private final ResourceService resourceService;

    /**
     * 更新资源审核状态（由audit-service调用）
     */
    @Operation(summary = "更新资源审核状态")
    @PutMapping("/{resourceId}/audit-status")
    public Result<Void> updateAuditStatus(
            @PathVariable Long resourceId,
            @RequestBody UpdateAuditStatusRequest request) {
        resourceService.updateAuditStatus(resourceId, request.getAuditStatus(),
                request.getAuditorId(), request.getAuditRemark());
        return Result.success();
    }

    /**
     * 批量获取资源信息（由course-service调用）
     */
    @Operation(summary = "批量获取资源信息")
    @PostMapping("/batch")
    public Result<java.util.List<com.edu.platform.resource.dto.response.ResourceResponse>> getResourcesByIds(
            @RequestBody java.util.List<Long> resourceIds) {
        if (cn.hutool.core.collection.CollUtil.isEmpty(resourceIds)) {
            return Result.success(new java.util.ArrayList<>());
        }
        
        return Result.success(resourceService.listResponsesByIds(resourceIds));
    }

    @Data
    public static class UpdateAuditStatusRequest {
        /** 审核状态: 1-通过, 2-拒绝 */
        private Integer auditStatus;
        /** 审核人ID */
        private Long auditorId;
        /** 审核备注 */
        private String auditRemark;
    }
}

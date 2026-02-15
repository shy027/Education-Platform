package com.edu.platform.course.controller.internal;

import com.edu.platform.common.annotation.RequireAdminOrLeader;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.internal.CoursewareInfoDTO;
import com.edu.platform.course.dto.internal.UpdateAuditStatusRequest;
import com.edu.platform.course.service.CoursewareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课件内部接口控制器(供其他服务调用)
 *
 * @author Education Platform
 */
@Tag(name = "课件内部接口")
@RestController
@RequestMapping("/internal/courseware")
@RequiredArgsConstructor
public class CoursewareInternalController {
    
    private final CoursewareService coursewareService;
    
    /**
     * 更新课件审核状态
     */
    @Operation(summary = "更新课件审核状态")
    @PutMapping("/{coursewareId}/audit-status")
    public Result<Void> updateAuditStatus(
            @PathVariable Long coursewareId,
            @RequestBody UpdateAuditStatusRequest request) {
        coursewareService.updateAuditStatus(coursewareId, request.getAuditStatus());
        return Result.success();
    }
    
    /**
     * 获取课件信息
     */
    @Operation(summary = "获取课件信息")
    @GetMapping("/{coursewareId}")
    public Result<CoursewareInfoDTO> getCoursewareInfo(@PathVariable Long coursewareId) {
        CoursewareInfoDTO info = coursewareService.getCoursewareInfo(coursewareId);
        return Result.success(info);
    }
    
}

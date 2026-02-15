package com.edu.platform.community.controller.internal;

import com.edu.platform.common.result.Result;
import com.edu.platform.community.dto.internal.PostInfoDTO;
import com.edu.platform.community.dto.internal.UpdateAuditStatusRequest;
import com.edu.platform.community.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子内部接口控制器(供其他服务调用)
 *
 * @author Education Platform
 */
@Tag(name = "帖子内部接口")
@RestController
@RequestMapping("/internal/posts")
@RequiredArgsConstructor
public class PostInternalController {
    
    private final PostService postService;
    
    /**
     * 更新帖子审核状态
     */
    @Operation(summary = "更新帖子审核状态")
    @PutMapping("/{postId}/audit-status")
    public Result<Void> updateAuditStatus(
            @PathVariable Long postId,
            @RequestBody UpdateAuditStatusRequest request) {
        postService.updateAuditStatus(postId, request.getAuditStatus());
        return Result.success();
    }
    
    /**
     * 获取帖子信息
     */
    @Operation(summary = "获取帖子信息")
    @GetMapping("/{postId}")
    public Result<PostInfoDTO> getPostInfo(@PathVariable Long postId) {
        PostInfoDTO info = postService.getPostInfo(postId);
        return Result.success(info);
    }
    
}

package com.edu.platform.community.controller.internal;

import com.edu.platform.common.result.Result;
import com.edu.platform.community.dto.internal.CommentInfoDTO;
import com.edu.platform.community.dto.internal.UpdateAuditStatusRequest;
import com.edu.platform.community.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评论内部接口控制器(供其他服务调用)
 *
 * @author Education Platform
 */
@Tag(name = "评论内部接口")
@RestController
@RequestMapping("/internal/comments")
@RequiredArgsConstructor
public class CommentInternalController {
    
    private final CommentService commentService;
    
    /**
     * 更新评论审核状态
     */
    @Operation(summary = "更新评论审核状态")
    @PutMapping("/{commentId}/audit-status")
    public Result<Void> updateAuditStatus(
            @PathVariable Long commentId,
            @RequestBody UpdateAuditStatusRequest request) {
        commentService.updateAuditStatus(commentId, request.getAuditStatus());
        return Result.success();
    }
    
    /**
     * 获取评论信息
     */
    @Operation(summary = "获取评论信息")
    @GetMapping("/{commentId}")
    public Result<CommentInfoDTO> getCommentInfo(@PathVariable Long commentId) {
        CommentInfoDTO info = commentService.getCommentInfo(commentId);
        return Result.success(info);
    }
    
}

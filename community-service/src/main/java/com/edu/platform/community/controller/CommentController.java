package com.edu.platform.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.Result;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.community.dto.request.CommentQueryRequest;
import com.edu.platform.community.dto.request.CreateCommentRequest;
import com.edu.platform.community.dto.response.CommentDetailResponse;
import com.edu.platform.community.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 观点管理控制器
 * 
 * 业务说明:
 * - 课程成员可以对话题发表观点
 * - 支持一级观点和二级回复
 * - 返回树形结构的观点列表
 *
 * @author Education Platform
 */
@Tag(name = "观点管理", description = "讨论观点相关接口")
@RestController
@RequestMapping("/api/v1/community/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    @Operation(summary = "发表观点", description = "对话题发表观点或回复他人观点")
    @PostMapping
    public Result<CommentDetailResponse> createComment(@Valid @RequestBody CreateCommentRequest request) {
        Long userId = UserContext.getUserId();
        CommentDetailResponse response = commentService.createComment(request, userId);
        return Result.success("发表成功", response);
    }
    
    @Operation(summary = "观点列表", description = "获取话题下的所有观点(树形结构)")
    @GetMapping
    public Result<Page<CommentDetailResponse>> listComments(
            @RequestParam Long postId,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        CommentQueryRequest request = new CommentQueryRequest();
        request.setPostId(postId);
        request.setParentId(parentId);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        Page<CommentDetailResponse> page = commentService.listComments(request);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "我的观点")
    @GetMapping("/my")
    public Result<Page<CommentDetailResponse>> listMyComments(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Long userId = UserContext.getUserId();
        CommentQueryRequest request = new CommentQueryRequest();
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        Page<CommentDetailResponse> page = commentService.listMyComments(request, userId);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "删除观点", description = "删除自己的观点或教师删除任何观点")
    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = UserContext.getUserId();
        commentService.deleteComment(commentId, userId);
        return Result.success("删除成功", null);
    }
    
}

package com.edu.platform.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.Result;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.community.dto.request.CreatePostRequest;
import com.edu.platform.community.dto.request.PostQueryRequest;
import com.edu.platform.community.dto.response.PostDetailResponse;
import com.edu.platform.community.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 话题管理控制器
 * 
 * 业务说明:
 * - 教师创建讨论话题(只需标题,内容可选)
 * - 学生和教师在话题下发表观点(通过评论接口)
 * - 支持一级讨论和二级回复
 *
 * @author Education Platform
 */
@Tag(name = "讨论话题管理", description = "课程讨论话题相关接口")
@RestController
@RequestMapping("/api/v1/community/posts")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;
    
    @Operation(summary = "创建讨论话题", description = "教师创建讨论话题,只需标题,内容可选")
    @PostMapping
    public Result<PostDetailResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        Long userId = UserContext.getUserId();
        PostDetailResponse response = postService.createPost(request, userId);
        return Result.success("话题创建成功", response);
    }
    
    @Operation(summary = "话题列表")
    @GetMapping
    public Result<Page<PostDetailResponse>> listPosts(
            @RequestParam Long courseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isTop,
            @RequestParam(required = false) Integer isEssence,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PostQueryRequest request = new PostQueryRequest();
        request.setCourseId(courseId);
        request.setKeyword(keyword);
        request.setIsTop(isTop);
        request.setIsEssence(isEssence);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        Page<PostDetailResponse> page = postService.listPosts(request);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "我的帖子")
    @GetMapping("/my")
    public Result<Page<PostDetailResponse>> listMyPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Long userId = UserContext.getUserId();
        PostQueryRequest request = new PostQueryRequest();
        request.setUserId(userId);
        request.setKeyword(keyword);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        Page<PostDetailResponse> page = postService.listPosts(request);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "我的点赞")
    @GetMapping("/my/likes")
    public Result<Page<PostDetailResponse>> listMyLikedPosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Long userId = UserContext.getUserId();
        PostQueryRequest request = new PostQueryRequest();
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        Page<PostDetailResponse> page = postService.listMyLikedPosts(request, userId);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "帖子详情")
    @GetMapping("/{postId}")
    public Result<PostDetailResponse> getPostDetail(@PathVariable Long postId) {
        PostDetailResponse response = postService.getPostDetail(postId);
        return Result.success("查询成功", response);
    }
    
    @Operation(summary = "编辑话题", description = "只有作者可以编辑话题")
    @PutMapping("/{postId}")
    public Result<PostDetailResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody com.edu.platform.community.dto.request.UpdatePostRequest request) {
        Long userId = UserContext.getUserId();
        PostDetailResponse response = postService.updatePost(postId, request, userId);
        return Result.success("编辑成功", response);
    }
    
    @Operation(summary = "删除话题", description = "作者或教师可以删除话题")
    @DeleteMapping("/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId) {
        Long userId = UserContext.getUserId();
        postService.deletePost(postId, userId);
        return Result.success("删除成功", null);
    }
    
    @Operation(summary = "置顶/取消置顶话题", description = "只有教师可以操作")
    @PutMapping("/{postId}/top")
    public Result<Void> toggleTop(
            @PathVariable Long postId,
            @RequestParam Integer isTop) {
        Long userId = UserContext.getUserId();
        postService.toggleTop(postId, isTop, userId);
        return Result.success(isTop == 1 ? "置顶成功" : "取消置顶成功", null);
    }
    
    @Operation(summary = "设为精华/取消精华", description = "只有教师可以操作")
    @PutMapping("/{postId}/essence")
    public Result<Void> toggleEssence(
            @PathVariable Long postId,
            @RequestParam Integer isEssence) {
        Long userId = UserContext.getUserId();
        postService.toggleEssence(postId, isEssence, userId);
        return Result.success(isEssence == 1 ? "设为精华成功" : "取消精华成功", null);
    }
    
}

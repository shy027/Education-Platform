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
    
    @Operation(summary = "帖子详情")
    @GetMapping("/{postId}")
    public Result<PostDetailResponse> getPostDetail(@PathVariable Long postId) {
        PostDetailResponse response = postService.getPostDetail(postId);
        return Result.success("查询成功", response);
    }
    
}

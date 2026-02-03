package com.edu.platform.community.controller;

import com.edu.platform.common.utils.UserContext;
import com.edu.platform.common.result.Result;
import com.edu.platform.community.dto.response.LikeResponse;
import com.edu.platform.community.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞控制器
 *
 * @author Education Platform
 */
@Tag(name = "点赞管理")
@RestController
@RequestMapping("/api/v1/community/likes")
@RequiredArgsConstructor
public class LikeController {
    
    private final LikeService likeService;
    
    @Operation(summary = "话题点赞/取消点赞")
    @PostMapping("/posts/{postId}")
    public Result<LikeResponse> togglePostLike(@PathVariable Long postId) {
        Long userId = UserContext.getUserId();
        LikeResponse response = likeService.togglePostLike(postId, userId);
        return Result.success(response);
    }
    
    @Operation(summary = "观点点赞/取消点赞")
    @PostMapping("/comments/{commentId}")
    public Result<LikeResponse> toggleCommentLike(@PathVariable Long commentId) {
        Long userId = UserContext.getUserId();
        LikeResponse response = likeService.toggleCommentLike(commentId, userId);
        return Result.success(response);
    }
}

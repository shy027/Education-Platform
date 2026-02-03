package com.edu.platform.community.service;

import com.edu.platform.community.dto.response.LikeResponse;

/**
 * 点赞服务接口
 *
 * @author Education Platform
 */
public interface LikeService {
    
    /**
     * 话题点赞/取消点赞
     * 
     * @param postId 话题ID
     * @param userId 用户ID
     * @return 点赞响应
     */
    LikeResponse togglePostLike(Long postId, Long userId);
    
    /**
     * 观点点赞/取消点赞
     * 
     * @param commentId 观点ID
     * @param userId 用户ID
     * @return 点赞响应
     */
    LikeResponse toggleCommentLike(Long commentId, Long userId);
}

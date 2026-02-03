package com.edu.platform.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.community.dto.response.LikeResponse;
import com.edu.platform.community.entity.CommunityComment;
import com.edu.platform.community.entity.CommunityCommentLike;
import com.edu.platform.community.entity.CommunityPost;
import com.edu.platform.community.entity.CommunityPostLike;
import com.edu.platform.community.mapper.CommunityCommentLikeMapper;
import com.edu.platform.community.mapper.CommunityCommentMapper;
import com.edu.platform.community.mapper.CommunityPostLikeMapper;
import com.edu.platform.community.mapper.CommunityPostMapper;
import com.edu.platform.community.service.LikeService;
import com.edu.platform.community.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    
    private final CommunityPostLikeMapper postLikeMapper;
    private final CommunityCommentLikeMapper commentLikeMapper;
    private final CommunityPostMapper postMapper;
    private final CommunityCommentMapper commentMapper;
    private final PermissionUtil permissionUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeResponse togglePostLike(Long postId, Long userId) {
        log.info("话题点赞/取消点赞, postId={}, userId={}", postId, userId);
        
        // 1. 验证话题是否存在
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("话题不存在");
        }
        
        // 2. 验证课程成员权限
        permissionUtil.checkCourseMember(userId, post.getCourseId());
        
        // 3. 查询点赞记录
        CommunityPostLike like = postLikeMapper.selectOne(
            new LambdaQueryWrapper<CommunityPostLike>()
                .eq(CommunityPostLike::getPostId, postId)
                .eq(CommunityPostLike::getUserId, userId)
        );
        
        boolean liked;
        if (like != null) {
            // 已点赞,取消点赞
            postLikeMapper.deleteById(like.getId());
            postMapper.update(null,
                new LambdaUpdateWrapper<CommunityPost>()
                    .eq(CommunityPost::getId, postId)
                    .setSql("like_count = like_count - 1")
            );
            liked = false;
            log.info("取消点赞成功, postId={}, userId={}", postId, userId);
        } else {
            // 未点赞,添加点赞
            like = new CommunityPostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            postLikeMapper.insert(like);
            
            postMapper.update(null,
                new LambdaUpdateWrapper<CommunityPost>()
                    .eq(CommunityPost::getId, postId)
                    .setSql("like_count = like_count + 1")
            );
            liked = true;
            log.info("点赞成功, postId={}, userId={}", postId, userId);
        }
        
        // 4. 返回结果
        post = postMapper.selectById(postId);
        return new LikeResponse(liked, post.getLikeCount());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeResponse toggleCommentLike(Long commentId, Long userId) {
        log.info("观点点赞/取消点赞, commentId={}, userId={}", commentId, userId);
        
        // 1. 验证观点是否存在
        CommunityComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("观点不存在");
        }
        
        // 2. 查询话题以获取courseId
        CommunityPost post = postMapper.selectById(comment.getPostId());
        if (post == null) {
            throw new BusinessException("话题不存在");
        }
        
        // 3. 验证课程成员权限
        permissionUtil.checkCourseMember(userId, post.getCourseId());
        
        // 4. 查询点赞记录
        CommunityCommentLike like = commentLikeMapper.selectOne(
            new LambdaQueryWrapper<CommunityCommentLike>()
                .eq(CommunityCommentLike::getCommentId, commentId)
                .eq(CommunityCommentLike::getUserId, userId)
        );
        
        boolean liked;
        if (like != null) {
            // 已点赞,取消点赞
            commentLikeMapper.deleteById(like.getId());
            commentMapper.update(null,
                new LambdaUpdateWrapper<CommunityComment>()
                    .eq(CommunityComment::getId, commentId)
                    .setSql("like_count = like_count - 1")
            );
            liked = false;
            log.info("取消点赞成功, commentId={}, userId={}", commentId, userId);
        } else {
            // 未点赞,添加点赞
            like = new CommunityCommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            
            commentMapper.update(null,
                new LambdaUpdateWrapper<CommunityComment>()
                    .eq(CommunityComment::getId, commentId)
                    .setSql("like_count = like_count + 1")
            );
            liked = true;
            log.info("点赞成功, commentId={}, userId={}", commentId, userId);
        }
        
        // 5. 返回结果
        comment = commentMapper.selectById(commentId);
        return new LikeResponse(liked, comment.getLikeCount());
    }
}

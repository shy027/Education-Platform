package com.edu.platform.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.community.dto.request.CommentQueryRequest;
import com.edu.platform.community.dto.request.CreateCommentRequest;
import com.edu.platform.community.dto.response.CommentDetailResponse;
import com.edu.platform.community.entity.CommunityComment;
import com.edu.platform.community.entity.CommunityPost;
import com.edu.platform.community.mapper.CommunityCommentMapper;
import com.edu.platform.community.mapper.CommunityPostMapper;
import com.edu.platform.community.client.UserServiceClient;
import com.edu.platform.community.dto.response.UserInfoDTO;
import com.edu.platform.community.service.CommentService;
import com.edu.platform.community.util.PermissionUtil;
import com.edu.platform.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {
    
    private final CommunityCommentMapper commentMapper;
    private final CommunityPostMapper postMapper;
    private final PermissionUtil permissionUtil;
    
    @Autowired(required = false)
    private UserServiceClient userServiceClient;
    
    @Autowired
    public CommentServiceImpl(CommunityCommentMapper commentMapper, 
                             CommunityPostMapper postMapper,
                             PermissionUtil permissionUtil) {
        this.commentMapper = commentMapper;
        this.postMapper = postMapper;
        this.permissionUtil = permissionUtil;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentDetailResponse createComment(CreateCommentRequest request, Long userId) {
        log.info("发表观点, postId={}, userId={}, parentId={}", 
                request.getPostId(), userId, request.getParentId());
        
        // 验证话题是否存在
        CommunityPost post = postMapper.selectById(request.getPostId());
        if (post == null) {
            throw new BusinessException("话题不存在");
        }
        
        // 验证用户是否为课程成员
        permissionUtil.checkCourseMember(userId, post.getCourseId());
        
        // 如果是二级回复,验证父评论是否存在
        if (request.getParentId() != 0) {
            CommunityComment parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null) {
                throw new BusinessException("回复的观点不存在");
            }
            // 只支持两级评论,不允许对二级评论再回复
            if (parentComment.getParentId() != 0) {
                throw new BusinessException("只支持两级评论");
            }
        }
        
        // 如果指定了被回复用户,验证该用户是否为课程成员
        if (request.getReplyToUserId() != null && request.getReplyToUserId() != 0) {
            try {
                permissionUtil.checkCourseMember(request.getReplyToUserId(), post.getCourseId());
            } catch (BusinessException e) {
                throw new BusinessException("被回复的用户不是该课程成员");
            }
        }
        
        // 创建评论实体
        CommunityComment comment = new CommunityComment();
        comment.setPostId(request.getPostId());
        comment.setUserId(userId);
        comment.setCommentContent(request.getCommentContent());
        comment.setParentId(request.getParentId());
        comment.setReplyToUserId(request.getReplyToUserId());
        comment.setLikeCount(0);
        comment.setStatus(1);
        
        // 保存到数据库
        commentMapper.insert(comment);
        
        // 更新话题的评论数
        postMapper.update(null,
            new LambdaUpdateWrapper<CommunityPost>()
                .eq(CommunityPost::getId, request.getPostId())
                .setSql("comment_count = comment_count + 1")
        );
        
        log.info("观点发表成功, commentId={}", comment.getId());
        
        // 返回详情
        return convertToResponse(comment);
    }
    
    @Override
    public Page<CommentDetailResponse> listComments(CommentQueryRequest request) {
        log.info("查询观点列表, postId={}, pageNum={}, pageSize={}", 
                request.getPostId(), request.getPageNum(), request.getPageSize());
        
        // 查询所有评论(包括一级和二级)
        LambdaQueryWrapper<CommunityComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityComment::getPostId, request.getPostId())
               .eq(CommunityComment::getStatus, 1); // 只查询正常状态
        
        // 排序
        if ("hot".equals(request.getOrderBy())) {
            wrapper.orderByDesc(CommunityComment::getLikeCount);
        } else {
            wrapper.orderByDesc(CommunityComment::getCreatedTime);
        }
        
        List<CommunityComment> allComments = commentMapper.selectList(wrapper);
        
        // 分离一级评论和二级评论
        List<CommunityComment> topLevelComments = allComments.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());
        
        Map<Long, List<CommunityComment>> repliesMap = allComments.stream()
                .filter(c -> c.getParentId() != 0)
                .collect(Collectors.groupingBy(CommunityComment::getParentId));
        
        // 手动分页一级评论
        int start = (request.getPageNum() - 1) * request.getPageSize();
        int end = Math.min(start + request.getPageSize(), topLevelComments.size());
        List<CommunityComment> pagedTopLevelComments = topLevelComments.subList(start, end);
        
        // 构建树形结构
        List<CommentDetailResponse> records = pagedTopLevelComments.stream()
                .map(comment -> {
                    CommentDetailResponse response = convertToResponse(comment);
                    // 添加子回复
                    List<CommunityComment> replies = repliesMap.getOrDefault(comment.getId(), new ArrayList<>());
                    response.setReplies(
                        replies.stream()
                            .map(this::convertToResponse)
                            .collect(Collectors.toList())
                    );
                    return response;
                })
                .collect(Collectors.toList());
        
        // 构建分页结果
        Page<CommentDetailResponse> page = new Page<>(request.getPageNum(), request.getPageSize());
        page.setRecords(records);
        page.setTotal(topLevelComments.size());
        
        return page;
    }

    @Override
    public Page<CommentDetailResponse> listMyComments(CommentQueryRequest request, Long userId) {
        log.info("查询我的观点, userId={}, pageNum={}, pageSize={}", userId, request.getPageNum(), request.getPageSize());
        
        // 构建查询条件
        LambdaQueryWrapper<CommunityComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityComment::getUserId, userId)
               .eq(CommunityComment::getStatus, 1)
               .orderByDesc(CommunityComment::getCreatedTime);
        
        // 分页查询
        Page<CommunityComment> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<CommunityComment> commentPage = commentMapper.selectPage(page, wrapper);
        
        // 转换为响应DTO (扁平结构)
        Page<CommentDetailResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(commentPage, responsePage, "records");
        responsePage.setRecords(
            commentPage.getRecords().stream()
                .map(this::convertToResponse)
                .toList()
        );
        
        return responsePage;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        log.info("删除观点, commentId={}, userId={}", commentId, userId);
        
        // 查询评论
        CommunityComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("观点不存在");
        }
        
        // 查询话题以获取courseId
        CommunityPost post = postMapper.selectById(comment.getPostId());
        if (post == null) {
            throw new BusinessException("话题不存在");
        }
        
        // 权限验证: 作者本人或教师可以删除
        permissionUtil.checkAuthorOrTeacher(userId, comment.getUserId(), post.getCourseId());
        
        // 如果是一级观点,级联删除所有二级回复
        if (comment.getParentId() == 0) {
            List<CommunityComment> replies = commentMapper.selectList(
                new LambdaQueryWrapper<CommunityComment>()
                    .eq(CommunityComment::getParentId, commentId)
            );
            
            if (!replies.isEmpty()) {
                // 批量删除二级回复
                List<Long> replyIds = replies.stream()
                    .map(CommunityComment::getId)
                    .collect(Collectors.toList());
                commentMapper.deleteByIds(replyIds);
                
                log.info("级联删除{}条二级回复", replies.size());
            }
        }
        
        // 逻辑删除观点本身
        commentMapper.deleteById(commentId);
        
        // 更新话题的评论数(包括级联删除的回复)
        int deleteCount = comment.getParentId() == 0 ? 
            commentMapper.selectCount(
                new LambdaQueryWrapper<CommunityComment>()
                    .eq(CommunityComment::getParentId, commentId)
            ).intValue() + 1 : 1;
        
        postMapper.update(null,
            new LambdaUpdateWrapper<CommunityPost>()
                .eq(CommunityPost::getId, comment.getPostId())
                .setSql("comment_count = comment_count - " + deleteCount)
        );
        
        log.info("观点删除成功, 共删除{}条记录", deleteCount);
    }
    
    /**
     * 转换为响应DTO
     */
    private CommentDetailResponse convertToResponse(CommunityComment comment) {
        CommentDetailResponse response = new CommentDetailResponse();
        BeanUtils.copyProperties(comment, response);
        
        // 批量查询用户信息
        List<Long> userIds = new ArrayList<>();
        userIds.add(comment.getUserId());
        if (comment.getReplyToUserId() != null) {
            userIds.add(comment.getReplyToUserId());
        }
        
        if (userServiceClient != null) {
            try {
                Result<Map<Long, UserInfoDTO>> result = userServiceClient.batchGetUserInfo(userIds);
                if (result != null && result.getData() != null) {
                    Map<Long, UserInfoDTO> userMap = result.getData();
                    
                    // 设置发表者信息
                    UserInfoDTO userInfo = userMap.get(comment.getUserId());
                    if (userInfo != null) {
                        response.setUserName(userInfo.getRealName());
                        response.setUserAvatar(userInfo.getAvatarUrl());
                    }
                    
                    // 设置被回复用户信息
                    if (comment.getReplyToUserId() != null) {
                        UserInfoDTO replyToUser = userMap.get(comment.getReplyToUserId());
                        if (replyToUser != null) {
                            response.setReplyToUserName(replyToUser.getRealName());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("查询用户信息失败, userId={}", comment.getUserId(), e);
                // 降级处理
                response.setUserName("用户" + comment.getUserId());
                response.setUserAvatar("");
                if (comment.getReplyToUserId() != null) {
                    response.setReplyToUserName("用户" + comment.getReplyToUserId());
                }
            }
        } else {
            // UserServiceClient不可用,使用占位符
            response.setUserName("用户" + comment.getUserId());
            response.setUserAvatar("");
            if (comment.getReplyToUserId() != null) {
                response.setReplyToUserName("用户" + comment.getReplyToUserId());
            }
        }
        
        return response;
    }
    
}

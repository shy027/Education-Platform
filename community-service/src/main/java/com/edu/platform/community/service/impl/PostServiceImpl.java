package com.edu.platform.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.community.dto.request.CreatePostRequest;
import com.edu.platform.community.dto.request.PostQueryRequest;
import com.edu.platform.community.dto.response.PostDetailResponse;
import com.edu.platform.community.entity.CommunityPost;
import com.edu.platform.community.mapper.CommunityPostMapper;
import com.edu.platform.community.client.UserServiceClient;
import com.edu.platform.community.dto.response.UserInfoDTO;
import com.edu.platform.community.service.PostService;
import com.edu.platform.community.util.PermissionUtil;
import com.edu.platform.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 话题服务实现
 * 
 * 业务说明:
 * - community_post表存储教师创建的讨论话题
 * - community_comment表存储所有人对话题的观点/讨论
 * - 话题只有标题是必填的,内容可选
 *
 * @author Education Platform
 */
@Slf4j
@Service
public class PostServiceImpl implements PostService {
    
    private final CommunityPostMapper postMapper;
    private final PermissionUtil permissionUtil;
    
    @Autowired(required = false)
    private UserServiceClient userServiceClient;
    
    @Autowired
    public PostServiceImpl(CommunityPostMapper postMapper, PermissionUtil permissionUtil) {
        this.postMapper = postMapper;
        this.permissionUtil = permissionUtil;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailResponse createPost(CreatePostRequest request, Long userId) {
        log.info("创建讨论话题, courseId={}, userId={}, title={}", 
                request.getCourseId(), userId, request.getPostTitle());
        
        // 验证用户是否为该课程的教师
        permissionUtil.checkTeacher(userId, request.getCourseId());
        
        // 创建话题实体
        CommunityPost post = new CommunityPost();
        post.setCourseId(request.getCourseId());
        post.setUserId(userId);
        post.setPostTitle(request.getPostTitle());
        post.setPostContent(request.getPostContent()); // 可以为空
        post.setAttachmentUrls(request.getAttachmentUrls());
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setAuditStatus(1); // 教师创建的话题默认通过
        post.setStatus(1); // 正常
        
        // 保存到数据库
        postMapper.insert(post);
        
        log.info("讨论话题创建成功, postId={}", post.getId());
        
        // TODO: 更新课程讨论数 course_info.discussion_count + 1
        // courseInfoMapper.update(null, 
        //     new LambdaUpdateWrapper<CourseInfo>()
        //         .eq(CourseInfo::getId, request.getCourseId())
        //         .setSql("discussion_count = discussion_count + 1")
        // );
        
        // 返回详情
        return getPostDetail(post.getId());
    }
    
    @Override
    public Page<PostDetailResponse> listPosts(PostQueryRequest request) {
        log.info("查询帖子列表, courseId={}, pageNum={}, pageSize={}", 
                request.getCourseId(), request.getPageNum(), request.getPageSize());
        
        // 构建查询条件
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getCourseId() != null, CommunityPost::getCourseId, request.getCourseId())
               .eq(request.getIsTop() != null, CommunityPost::getIsTop, request.getIsTop())
               .eq(request.getIsEssence() != null, CommunityPost::getIsEssence, request.getIsEssence())
               .eq(CommunityPost::getStatus, 1) // 只查询正常状态
               .eq(CommunityPost::getAuditStatus, 1); // 只查询已审核通过
        
        // 排序
        if ("hot".equals(request.getOrderBy())) {
            wrapper.orderByDesc(CommunityPost::getViewCount)
                   .orderByDesc(CommunityPost::getLikeCount);
        } else {
            wrapper.orderByDesc(CommunityPost::getCreatedTime);
        }
        
        // 分页查询
        Page<CommunityPost> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<CommunityPost> postPage = postMapper.selectPage(page, wrapper);
        
        // 转换为响应DTO
        Page<PostDetailResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(postPage, responsePage, "records");
        responsePage.setRecords(
            postPage.getRecords().stream()
                .map(this::convertToResponse)
                .toList()
        );
        
        return responsePage;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailResponse getPostDetail(Long postId) {
        log.info("查询帖子详情, postId={}", postId);
        
        // 查询帖子
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        // 浏览次数+1
        postMapper.update(null,
            new LambdaUpdateWrapper<CommunityPost>()
                .eq(CommunityPost::getId, postId)
                .setSql("view_count = view_count + 1")
        );
        post.setViewCount(post.getViewCount() + 1);
        
        // 转换为响应DTO
        return convertToResponse(post);
    }
    
    /**
     * 转换为响应DTO
     */
    private PostDetailResponse convertToResponse(CommunityPost post) {
        PostDetailResponse response = new PostDetailResponse();
        BeanUtils.copyProperties(post, response);
        
        // 查询用户信息
        if (userServiceClient != null) {
            try {
                Result<Map<Long, UserInfoDTO>> result = userServiceClient.batchGetUserInfo(
                    java.util.Collections.singletonList(post.getUserId())
                );
                if (result != null && result.getData() != null) {
                    UserInfoDTO userInfo = result.getData().get(post.getUserId());
                    if (userInfo != null) {
                        response.setUserName(userInfo.getRealName());
                        response.setUserAvatar(userInfo.getAvatarUrl());
                    }
                }
            } catch (Exception e) {
                log.warn("查询用户信息失败, userId={}", post.getUserId(), e);
                // 降级处理
                response.setUserName("用户" + post.getUserId());
                response.setUserAvatar("");
            }
        } else {
            // UserServiceClient不可用,使用占位符
            response.setUserName("用户" + post.getUserId());
            response.setUserAvatar("");
        }
        
        return response;
    }
    
}

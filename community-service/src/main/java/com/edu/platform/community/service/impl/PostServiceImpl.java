package com.edu.platform.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.community.dto.request.CreatePostRequest;
import com.edu.platform.community.dto.request.PostQueryRequest;
import com.edu.platform.community.dto.request.UpdatePostRequest;
import com.edu.platform.community.dto.response.PostDetailResponse;
import com.edu.platform.community.entity.CommunityPost;
import com.edu.platform.community.entity.CommunityComment;
import com.edu.platform.community.mapper.CommunityPostMapper;
import com.edu.platform.community.client.UserServiceClient;
import com.edu.platform.community.dto.response.UserInfoDTO;
import com.edu.platform.community.service.PostService;
import com.edu.platform.community.util.PermissionUtil;
import com.edu.platform.common.result.Result;
import com.edu.platform.community.mq.AuditRequestSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final com.edu.platform.community.mapper.CommunityPostLikeMapper postLikeMapper;
    private final UserServiceClient userServiceClient;
    private final AuditRequestSender auditRequestSender;
    private final com.edu.platform.community.client.BehaviorClient behaviorClient;
    private final com.edu.platform.community.mapper.CommunityCommentMapper commentMapper;
    
    @Autowired
    public PostServiceImpl(CommunityPostMapper postMapper, 
                           PermissionUtil permissionUtil,
                           com.edu.platform.community.mapper.CommunityPostLikeMapper postLikeMapper,
                           @Autowired(required = false) UserServiceClient userServiceClient,
                           @Autowired(required = false) AuditRequestSender auditRequestSender,
                           @Autowired(required = false) com.edu.platform.community.client.BehaviorClient behaviorClient,
                           com.edu.platform.community.mapper.CommunityCommentMapper commentMapper) {
        this.postMapper = postMapper;
        this.permissionUtil = permissionUtil;
        this.postLikeMapper = postLikeMapper;
        this.userServiceClient = userServiceClient;
        this.auditRequestSender = auditRequestSender;
        this.behaviorClient = behaviorClient;
        this.commentMapper = commentMapper;
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
        
        // 发送审核记录消息（教师帖子默认通过）
        if (auditRequestSender != null) {
            auditRequestSender.sendPostAuditRequest(
                    post.getId(),
                    userId,
                    request.getPostTitle(),
                    request.getPostContent()
            );
        }
        
        log.info("讨论话题创建成功, postId={}", post.getId());

        // 上报行为日志
        if (behaviorClient != null) {
            try {
                behaviorClient.logBehavior(com.edu.platform.common.dto.BehaviorLogDTO.builder()
                        .userId(userId)
                        .courseId(request.getCourseId())
                        .behaviorType("POST_COMMENT")
                        .behaviorObjectId(post.getId())
                        .behaviorData(cn.hutool.json.JSONUtil.createObj()
                                .set("title", post.getPostTitle())
                                .set("isPost", true)
                                .toString())
                        .build());
            } catch (Exception e) {
                log.error("上报帖子创建行为失败", e);
            }
        }
        
        // TODO: 更新课程讨论数 course_info.discussion_count + 1
        // courseInfoMapper.update(null, 
        //     new LambdaUpdateWrapper<CourseInfo>()
        //         .eq(CourseInfo::getId, request.getCourseId())
        //         .setSql("discussion_count = discussion_count + 1")
        // );
        
        // 返回详情
        return getPostDetail(post.getId(), userId);
    }
    
    @Override
    public Page<PostDetailResponse> listPosts(PostQueryRequest request, Long userId) {
        log.info("查询帖子列表, courseId={}, pageNum={}, pageSize={}", 
                request.getCourseId(), request.getPageNum(), request.getPageSize());
        
        // 构建查询条件
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getCourseId() != null, CommunityPost::getCourseId, request.getCourseId())
               .eq(request.getUserId() != null, CommunityPost::getUserId, request.getUserId()) // 增加用户ID筛选
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
                .map(post -> convertToResponse(post, userId))
                .toList()
        );
        
        return responsePage;
    }
    
    @Override
    public Page<PostDetailResponse> listMyLikedPosts(PostQueryRequest request, Long userId) {
        log.info("查询我的点赞, userId={}, pageNum={}, pageSize={}", userId, request.getPageNum(), request.getPageSize());
        
        // 构建查询条件
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPost::getStatus, 1)
               .eq(CommunityPost::getAuditStatus, 1)
               // 使用子查询: id IN (SELECT post_id FROM community_post_like WHERE user_id = ?)
               .inSql(CommunityPost::getId, "SELECT post_id FROM community_post_like WHERE user_id = " + userId)
               .orderByDesc(CommunityPost::getCreatedTime);
        
        // 分页查询
        Page<CommunityPost>page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<CommunityPost> postPage = postMapper.selectPage(page, wrapper);
        
        // 转换为响应DTO
        Page<PostDetailResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(postPage, responsePage, "records");
        responsePage.setRecords(
            postPage.getRecords().stream()
                .map(post -> convertToResponse(post, userId))
                .toList()
        );
        
        return responsePage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailResponse getPostDetail(Long postId, Long userId) {
        log.info("查询帖子详情, postId={}, userId={}", postId, userId);
        
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
        return convertToResponse(post, userId);
    }
    
    /**
     * 转换为响应DTO
     */
    private PostDetailResponse convertToResponse(CommunityPost post, Long userId) {
        PostDetailResponse response = new PostDetailResponse();
        BeanUtils.copyProperties(post, response);
        
        // 检查当前用户是否已点赞
        if (userId != null && postLikeMapper != null) {
            com.edu.platform.community.entity.CommunityPostLike like = postLikeMapper.selectOne(
                new LambdaQueryWrapper<com.edu.platform.community.entity.CommunityPostLike>()
                    .eq(com.edu.platform.community.entity.CommunityPostLike::getPostId, post.getId())
                    .eq(com.edu.platform.community.entity.CommunityPostLike::getUserId, userId)
            );
            response.setLiked(like != null);
        } else {
            response.setLiked(false);
        }
        
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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailResponse updatePost(Long postId, UpdatePostRequest request, Long userId) {
        log.info("编辑话题, postId={}, userId={}", postId, userId);
        
        // 1. 查询话题
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("话题不存在");
        }
        
        // 2. 验证作者权限
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("只有作者可以编辑话题");
        }
        
        // 3. 更新话题
        post.setPostTitle(request.getPostTitle());
        post.setPostContent(request.getPostContent());
        post.setAttachmentUrls(request.getAttachmentUrls());
        postMapper.updateById(post);
        
        log.info("话题编辑成功, postId={}", postId);
        
        // 4. 返回详情
        return getPostDetail(postId, userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long userId) {
        log.info("删除话题, postId={}, userId={}", postId, userId);
        
        // 1. 查询话题
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("话题不存在");
        }
        
        // 2. 验证权限(作者或教师)
        permissionUtil.checkAuthorOrTeacher(userId, post.getUserId(), post.getCourseId());
        
        // 3. 逻辑删除话题
        postMapper.deleteById(postId);
        
        log.info("话题删除成功, postId={}", postId);
        
        // TODO: 级联删除所有观点
        // TODO: 更新课程讨论数
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTop(Long postId, Integer isTop, Long userId) {
        log.info("置顶话题, postId={}, isTop={}, userId={}", postId, isTop, userId);
        
        // 1. 查询话题
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("话题不存在");
        }
        
        // 2. 验证教师权限
        permissionUtil.checkTeacher(userId, post.getCourseId());
        
        // 3. 更新置顶状态
        post.setIsTop(isTop);
        postMapper.updateById(post);
        
        log.info("话题置顶状态更新成功, postId={}, isTop={}", postId, isTop);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleEssence(Long postId, Integer isEssence, Long userId) {
        log.info("设置精华, postId={}, isEssence={}, userId={}", postId, isEssence, userId);
        
        // 1. 查询话题
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("话题不存在");
        }
        
        // 2. 验证教师权限
        permissionUtil.checkTeacher(userId, post.getCourseId());
        
        // 3. 更新精华状态
        post.setIsEssence(isEssence);
        postMapper.updateById(post);
        
        log.info("话题精华状态更新成功, postId={}, isEssence={}", postId, isEssence);

        // 如果设置为精华，为帖主及所有参与评论的学生加分
        if (behaviorClient != null) {
            try {
                if (isEssence == 1) {
                    // 1. 收集所有相关用户ID (帖主 + 评论者)
                    java.util.Set<Long> userIds = new java.util.HashSet<>();
                    if (post.getUserId() != null) {
                        userIds.add(post.getUserId());
                    } else {
                        log.warn("话题加精排查 - 帖子作者ID为空: postId={}", post.getId());
                    }
                    
                    // 2. 查询所有在该话题下留言的用户 (使用 String 避免 Lambda 潜在问题)
                    com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CommunityComment> wrapper = 
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                    wrapper.eq("post_id", post.getId());
                    
                    java.util.List<CommunityComment> comments = commentMapper.selectList(wrapper);
                    
                    log.info("话题加精排查 - 数据库查询结果: postId={}, 原始评论数: {}", post.getId(), comments != null ? comments.size() : 0);
                    if (comments != null) {
                        for (CommunityComment comment : comments) {
                            if (comment.getUserId() != null) {
                                userIds.add(comment.getUserId());
                                log.info("话题加精排查 - 发现留言者: userId={}, status={}, auditStatus={}", 
                                    comment.getUserId(), comment.getStatus(), comment.getAuditStatus());
                            }
                        }
                    }

                    // 3. 过滤出学生账号 (批量查询角色信息)
                    java.util.Set<Long> studentIds = new java.util.HashSet<>();
                    log.info("话题加精排查 - 进入合规性检查, 涉及唯一用户数: {}, IDs: {}", userIds.size(), userIds);
                    
                    if (!userIds.isEmpty()) {
                        if (userServiceClient != null) {
                            try {
                                com.edu.platform.common.result.Result<java.util.Map<Long, com.edu.platform.community.dto.response.UserInfoDTO>> userResult = 
                                    userServiceClient.batchGetUserInfo(new java.util.ArrayList<>(userIds));
                                
                                if (userResult != null && userResult.getData() != null) {
                                    java.util.Map<Long, com.edu.platform.community.dto.response.UserInfoDTO> userMap = userResult.getData();
                                    log.info("话题加精排查 - 用户中心返回条数: {}", userMap.size());
                                    
                                    for (Long uid : userIds) {
                                        com.edu.platform.community.dto.response.UserInfoDTO userInfo = userMap.get(uid);
                                        if (userInfo != null) {
                                            List<com.edu.platform.community.dto.response.UserInfoDTO.RoleInfo> roles = userInfo.getRoles();
                                            log.info("话题加精排查 - 校验用户角色列表: userId={}, name={}, 角色数={}", 
                                                uid, userInfo.getRealName(), roles != null ? roles.size() : 0);
                                            
                                            boolean isStudent = false;
                                            if (roles != null) {
                                                for (com.edu.platform.community.dto.response.UserInfoDTO.RoleInfo role : roles) {
                                                    String rn = role.getRoleName() != null ? role.getRoleName() : "";
                                                    String rc = role.getRoleCode() != null ? role.getRoleCode() : "";
                                                    log.info("话题加精排查 - 检查具体角色: userId={}, roleName={}, roleCode={}", uid, rn, rc);
                                                    
                                                    if (rn.contains("学生") || rc.toUpperCase().contains("STUDENT")) {
                                                        isStudent = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            
                                            if (isStudent) {
                                                studentIds.add(uid);
                                            }
                                        } else {
                                            log.warn("话题加精排查 - 无法获取用户信息: userId={}", uid);
                                        }
                                    }
                                } else {
                                    log.error("话题加精排查 - 用户服务返回 Data 为空");
                                }
                            } catch (Exception e) {
                                log.error("话题加精排查 - 调用用户服务异常", e);
                                studentIds = userIds; // 降级策略
                            }
                        } else {
                            log.error("话题加精排查 - userServiceClient 未注入，无法检查学生身份！");
                        }
                    }

                    // 4. 批量上报行为权重
                    log.info("话题加精画像同步: postId={}, 合资格学生总数={}", post.getId(), studentIds.size());
                    for (Long targetUserId : studentIds) {
                        behaviorClient.logBehavior(com.edu.platform.common.dto.BehaviorLogDTO.builder()
                                .userId(targetUserId)
                                .courseId(post.getCourseId())
                                .behaviorType("ESSENCE_POST")
                                .behaviorObjectId(post.getId())
                                .behaviorData(cn.hutool.json.JSONUtil.createObj()
                                        .set("title", post.getPostTitle())
                                        .toString())
                                .build());
                    }
                } else {
                    // 取消精华，同步删除画像系统中的加分埋点记录 (该接口会根据 type 和 objectId 批量删除，涵盖所有涉及用户)
                    log.info("话题取消精华画像同步: postId={}", post.getId());
                    behaviorClient.deleteBehavior("ESSENCE_POST", post.getId());
                }
            } catch (Exception e) {
                log.error("操作精华帖画像日志失败, isEssence={}", isEssence, e);
            }
        }
    }
    
    @Override
    public void updateAuditStatus(Long postId, Integer auditStatus) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        post.setAuditStatus(auditStatus);
        postMapper.updateById(post);
        
        log.info("更新帖子审核状态: postId={}, auditStatus={}", postId, auditStatus);
    }
    
    @Override
    public com.edu.platform.community.dto.internal.PostInfoDTO getPostInfo(Long postId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        com.edu.platform.community.dto.internal.PostInfoDTO dto = 
            new com.edu.platform.community.dto.internal.PostInfoDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getPostTitle());
        dto.setContent(post.getPostContent());
        dto.setAuthorId(post.getUserId());
        
        // TODO: 通过OpenFeign获取作者姓名
        dto.setAuthorName("用户" + post.getUserId());
        
        return dto;
    }
    
}

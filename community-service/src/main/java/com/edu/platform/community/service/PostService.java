package com.edu.platform.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.community.dto.request.CreatePostRequest;
import com.edu.platform.community.dto.request.PostQueryRequest;
import com.edu.platform.community.dto.request.UpdatePostRequest;
import com.edu.platform.community.dto.response.PostDetailResponse;

/**
 * 话题服务接口
 * 
 * 业务逻辑:
 * - 教师创建讨论话题(标题必填,内容可选)
 * - 所有课程成员可查看话题列表和详情
 * - 通过评论接口发表观点和回复
 *
 * @author Education Platform
 */
public interface PostService {
    
    /**
     * 创建讨论话题(仅教师)
     * 
     * @param request 话题信息(标题必填,内容可选)
     * @param userId 创建者ID(教师)
     * @return 话题详情
     */
    PostDetailResponse createPost(CreatePostRequest request, Long userId);
    
    /**
     * 话题列表
     * 
     * @param request 查询条件
     * @return 分页结果
     */
    Page<PostDetailResponse> listPosts(PostQueryRequest request);
    
    /**
     * 话题详情
     * 
     * @param postId 话题ID
     * @return 话题详情
     */
    PostDetailResponse getPostDetail(Long postId);
    
    /**
     * 编辑话题
     * 
     * @param postId 话题ID
     * @param request 更新内容
     * @param userId 用户ID
     * @return 更新后的话题详情
     */
    PostDetailResponse updatePost(Long postId, UpdatePostRequest request, Long userId);
    
    /**
     * 删除话题
     * 
     * @param postId 话题ID
     * @param userId 用户ID
     */
    void deletePost(Long postId, Long userId);
    
    /**
     * 置顶/取消置顶话题
     * 
     * @param postId 话题ID
     * @param isTop 1:置顶, 0:取消置顶
     * @param userId 用户ID
     */
    void toggleTop(Long postId, Integer isTop, Long userId);
    
    /**
     * 设为精华/取消精华
     * 
     * @param postId 话题ID
     * @param isEssence 1:精华, 0:取消精华
     * @param userId 用户ID
     */
    void toggleEssence(Long postId, Integer isEssence, Long userId);
    
}

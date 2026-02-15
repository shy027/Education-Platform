package com.edu.platform.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.community.dto.internal.CommentInfoDTO;
import com.edu.platform.community.dto.request.CommentQueryRequest;
import com.edu.platform.community.dto.request.CreateCommentRequest;
import com.edu.platform.community.dto.response.CommentDetailResponse;

/**
 * 评论服务接口
 * 
 * 业务逻辑:
 * - 课程成员可以对话题发表观点(一级评论)
 * - 成员之间可以互相回复(二级评论)
 * - 支持树形结构展示
 *
 * @author Education Platform
 */
public interface CommentService {
    
    /**
     * 发表观点
     * 
     * @param request 观点信息
     * @param userId 发表者ID
     * @return 观点详情
     */
    CommentDetailResponse createComment(CreateCommentRequest request, Long userId);
    
    /**
     * 观点列表(树形结构)
     * 
     * @param request 查询条件
     * @return 分页结果(只返回一级观点,每个观点包含其回复列表)
     */
    Page<CommentDetailResponse> listComments(CommentQueryRequest request);
    
    /**
     * 查询我的观点
     *
     * @param request 分页参数
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<CommentDetailResponse> listMyComments(CommentQueryRequest request, Long userId);
    
    /**
     * 删除观点
     * 
     * @param commentId 观点ID
     * @param userId 操作者ID
     */
    void deleteComment(Long commentId, Long userId);
    
    /**
     * 更新审核状态(内部调用)
     */
    void updateAuditStatus(Long commentId, Integer auditStatus);
    
    /**
     * 获取评论信息(内部调用)
     */
    CommentInfoDTO getCommentInfo(Long commentId);
    
}

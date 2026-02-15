package com.edu.platform.audit.client;

import com.edu.platform.audit.dto.feign.CommentInfoDTO;
import com.edu.platform.audit.dto.feign.PostInfoDTO;
import com.edu.platform.audit.dto.feign.UpdateAuditStatusRequest;
import com.edu.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 社区服务Feign客户端
 *
 * @author Education Platform
 */
@FeignClient(name = "community-service", url = "http://localhost:8083", path = "/internal")
public interface CommunityClient {
    
    /**
     * 更新帖子审核状态
     *
     * @param postId 帖子ID
     * @param request 审核状态请求
     * @return 结果
     */
    @PutMapping("/posts/{postId}/audit-status")
    Result<Void> updatePostAuditStatus(
        @PathVariable("postId") Long postId,
        @RequestBody UpdateAuditStatusRequest request
    );
    
    /**
     * 更新评论审核状态
     *
     * @param commentId 评论ID
     * @param request 审核状态请求
     * @return 结果
     */
    @PutMapping("/comments/{commentId}/audit-status")
    Result<Void> updateCommentAuditStatus(
        @PathVariable("commentId") Long commentId,
        @RequestBody UpdateAuditStatusRequest request
    );
    
    /**
     * 获取帖子信息
     *
     * @param postId 帖子ID
     * @return 帖子信息
     */
    @GetMapping("/posts/{postId}")
    Result<PostInfoDTO> getPostInfo(@PathVariable("postId") Long postId);
    
    /**
     * 获取评论信息
     *
     * @param commentId 评论ID
     * @return 评论信息
     */
    @GetMapping("/comments/{commentId}")
    Result<CommentInfoDTO> getCommentInfo(@PathVariable("commentId") Long commentId);
    
}

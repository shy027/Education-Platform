package com.edu.platform.community.mq;

import com.edu.platform.common.dto.AuditRequestMessage;
import com.edu.platform.community.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 审核请求消息发送者 - community-service
 *
 * @author Education Platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditRequestSender {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送帖子审核请求 (教师创建，默认通过)
     *
     * @param postId    帖子ID
     * @param creatorId 创建者ID
     * @param title     帖子标题
     * @param content   帖子内容
     */
    public void sendPostAuditRequest(Long postId, Long creatorId, String title, String content) {
        String preview = content != null && content.length() > 100
                ? content.substring(0, 100) + "..."
                : content;

        AuditRequestMessage message = AuditRequestMessage.builder()
                .contentType("POST")
                .contentId(postId)
                .creatorId(creatorId)
                .contentTitle(title)
                .contentPreview(preview)
                .initialAuditResult(1)   // 教师帖子默认通过
                .auditReason("教师发布，默认通过")
                .build();
        sendAuditRequest(message);
    }

    /**
     * 发送评论审核请求 (默认通过)
     *
     * @param commentId 评论ID
     * @param creatorId 创建者ID
     * @param content   评论内容
     */
    public void sendCommentAuditRequest(Long commentId, Long creatorId, String content) {
        // 内容预览截取前100字
        String preview = content != null && content.length() > 100
                ? content.substring(0, 100) + "..."
                : content;

        AuditRequestMessage message = AuditRequestMessage.builder()
                .contentType("COMMENT")
                .contentId(commentId)
                .creatorId(creatorId)
                .contentTitle("评论#" + commentId)
                .contentPreview(preview)
                .initialAuditResult(1)   // 评论默认通过
                .auditReason("默认通过")
                .build();
        sendAuditRequest(message);
    }

    /**
     * 发送审核请求消息
     */
    public void sendAuditRequest(AuditRequestMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.AUDIT_REQUEST_EXCHANGE,
                    RabbitMQConfig.AUDIT_REQUEST_ROUTING_KEY,
                    message
            );
            log.info("发送审核请求成功: contentType={}, contentId={}",
                    message.getContentType(), message.getContentId());
        } catch (Exception e) {
            log.error("发送审核请求失败: contentType={}, contentId={}, error={}",
                    message.getContentType(), message.getContentId(), e.getMessage(), e);
        }
    }
}

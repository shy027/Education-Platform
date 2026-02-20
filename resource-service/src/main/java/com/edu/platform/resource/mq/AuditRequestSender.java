package com.edu.platform.resource.mq;

import com.edu.platform.common.dto.AuditRequestMessage;
import com.edu.platform.resource.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 审核请求消息发送者 - resource-service
 *
 * @author Education Platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditRequestSender {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 教师提交资源审核（需人工审核）
     *
     * @param resourceId 资源ID
     * @param creatorId  创建者ID
     * @param title      资源标题
     * @param summary    资源摘要
     */
    public void sendResourceAuditRequest(Long resourceId, Long creatorId, String title, String summary) {
        String preview = summary != null && summary.length() > 100
                ? summary.substring(0, 100) + "..."
                : summary;

        AuditRequestMessage message = AuditRequestMessage.builder()
                .contentType("RESOURCE")
                .contentId(resourceId)
                .creatorId(creatorId)
                .contentTitle(title)
                .contentPreview(preview)
                .initialAuditResult(0)   // 教师资源需人工审核
                .build();
        sendAuditRequest(message);
    }

    /**
     * 管理员发布资源（默认通过）
     *
     * @param resourceId 资源ID
     * @param creatorId  创建者ID
     * @param title      资源标题
     * @param summary    资源摘要
     */
    public void sendAdminResourceAuditRequest(Long resourceId, Long creatorId, String title, String summary) {
        String preview = summary != null && summary.length() > 100
                ? summary.substring(0, 100) + "..."
                : summary;

        AuditRequestMessage message = AuditRequestMessage.builder()
                .contentType("RESOURCE")
                .contentId(resourceId)
                .creatorId(creatorId)
                .contentTitle(title)
                .contentPreview(preview)
                .initialAuditResult(1)   // 管理员发布默认通过
                .auditReason("管理员发布，默认通过")
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
            log.info("发送资源审核请求成功: contentType={}, contentId={}",
                    message.getContentType(), message.getContentId());
        } catch (Exception e) {
            log.error("发送资源审核请求失败: contentType={}, contentId={}, error={}",
                    message.getContentType(), message.getContentId(), e.getMessage(), e);
        }
    }
}

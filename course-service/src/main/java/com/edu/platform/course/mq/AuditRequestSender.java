package com.edu.platform.course.mq;

import com.edu.platform.common.dto.AuditRequestMessage;
import com.edu.platform.course.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 审核请求消息发送者
 * 课件上传后发送消息给audit-service自动创建审核记录
 *
 * @author Education Platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditRequestSender {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送课件审核请求
     *
     * @param coursewareId 课件ID
     * @param creatorId    创建者ID
     * @param title        课件标题
     * @param preview      预览内容(描述)
     */
    public void sendCoursewareAuditRequest(Long coursewareId, Long creatorId,
                                            String title, String preview) {
        AuditRequestMessage message = AuditRequestMessage.builder()
                .contentType("COURSEWARE")
                .contentId(coursewareId)
                .creatorId(creatorId)
                .contentTitle(title)
                .contentPreview(preview)
                .build();
        sendAuditRequest(message);
    }

    /**
     * 发送审核请求消息
     *
     * @param message 审核请求消息
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
            // 发送失败不影响主流程,仅记录日志
        }
    }
}

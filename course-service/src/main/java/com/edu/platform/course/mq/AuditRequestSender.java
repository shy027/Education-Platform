package com.edu.platform.course.mq;

import com.edu.platform.common.dto.AuditRequestMessage;
import com.edu.platform.course.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 审核请求消息发送者 - course-service
 *
 * @author Education Platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditRequestSender {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 教师提交课程审核
     *
     * @param courseId  课程ID
     * @param teacherId 教师ID
     * @param title     课程标题
     * @param summary   课程摘要
     */
    public void sendCourseAuditRequest(Long courseId, Long teacherId, String title, String summary) {
        String preview = summary != null && summary.length() > 100
                ? summary.substring(0, 100) + "..."
                : summary;

        AuditRequestMessage message = AuditRequestMessage.builder()
                .contentType("COURSE")
                .contentId(courseId)
                .creatorId(teacherId)
                .contentTitle(title)
                .contentPreview(preview)
                .initialAuditResult(0)   // 待人工审核
                .build();
        sendAuditRequest(message);
    }

    /**
     * 课件上传/更新审核（通常为自动审核）
     *
     * @param coursewareId 课件ID
     * @param creatorId    创建者ID
     * @param title        课件标题
     * @param summary      课件摘要/描述
     * @param initialResult 初始审核结果: 0-待定, 1-通过, 2-不通过
     * @param reason       原因
     */
    public void sendCoursewareAuditRequest(Long coursewareId, Long creatorId, String title, String summary, Integer initialResult, String reason) {
        String preview = summary != null && summary.length() > 100
                ? summary.substring(0, 100) + "..."
                : summary;

        AuditRequestMessage message = AuditRequestMessage.builder()
                .contentType("COURSEWARE")
                .contentId(coursewareId)
                .creatorId(creatorId)
                .contentTitle(title)
                .contentPreview(preview)
                .initialAuditResult(initialResult)
                .auditReason(reason)
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
            log.info("发送课程审核请求成功: contentType={}, contentId={}",
                    message.getContentType(), message.getContentId());
        } catch (Exception e) {
            log.error("发送课程审核请求失败: contentType={}, contentId={}, error={}",
                    message.getContentType(), message.getContentId(), e.getMessage(), e);
        }
    }
}

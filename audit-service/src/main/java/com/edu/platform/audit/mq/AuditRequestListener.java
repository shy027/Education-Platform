package com.edu.platform.audit.mq;

import com.edu.platform.audit.entity.AuditRecord;
import com.edu.platform.audit.mapper.AuditRecordMapper;
import com.edu.platform.audit.config.RabbitMQConfig;
import com.edu.platform.common.dto.AuditRequestMessage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 审核请求消息监听者
 * 监听RabbitMQ消息,自动为新内容创建审核记录
 *
 * @author Education Platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditRequestListener {

    private final AuditRecordMapper auditRecordMapper;

    /**
     * 处理审核请求消息
     *
     * @param message 审核请求消息
     */
    @RabbitListener(queues = RabbitMQConfig.AUDIT_REQUEST_QUEUE)
    public void handleAuditRequest(AuditRequestMessage message) {
        log.info("收到审核请求: contentType={}, contentId={}",
                message.getContentType(), message.getContentId());

        try {
            // 检查是否已存在审核记录(幂等处理)
            Long existCount = auditRecordMapper.selectCount(
                    new LambdaQueryWrapper<AuditRecord>()
                            .eq(AuditRecord::getContentType, message.getContentType())
                            .eq(AuditRecord::getContentId, message.getContentId())
            );

            if (existCount > 0) {
                log.warn("审核记录已存在,跳过创建: contentType={}, contentId={}",
                        message.getContentType(), message.getContentId());
                return;
            }

            // 创建审核记录
            AuditRecord record = new AuditRecord();
            record.setContentType(message.getContentType());
            record.setContentId(message.getContentId());
            record.setAuditMethod(1);   // 1-AI/系统审核

            // 根据消息携带的初始审核结果设置状态
            // 课件(COURSEWARE)=0待审核, 帖子(POST)/评论(COMMENT)=1默认通过
            Integer initialResult = message.getInitialAuditResult();
            if (initialResult != null && initialResult == 1) {
                // 默认通过
                record.setAuditResult(1);
                record.setAuditReason(message.getAuditReason() != null
                        ? message.getAuditReason() : "默认通过");
                record.setAuditTime(java.time.LocalDateTime.now());
            } else {
                // 待人工审核
                record.setAuditResult(0);
                record.setRiskLevel(assessRiskLevel(message));
                record.setAiConfidence(new BigDecimal("0.85"));
            }

            try {
                auditRecordMapper.insert(record);
                log.info("审核记录创建成功: recordId={}, contentType={}, contentId={}",
                        record.getId(), message.getContentType(), message.getContentId());
            } catch (org.springframework.dao.DuplicateKeyException e) {
                log.warn("并发冲突：记录已被其他请求（如Feign）抢先创建: contentType={}, contentId={}",
                        message.getContentType(), message.getContentId());
            }

        } catch (Exception e) {
            log.error("处理审核请求失败: contentType={}, contentId={}, error={}",
                    message.getContentType(), message.getContentId(), e.getMessage(), e);
            // 抛出异常触发消息重试
            throw new RuntimeException("处理审核请求失败", e);
        }
    }

    /**
     * 模拟AI风险等级评估
     * 实际项目中应调用真实的AI审核API
     *
     * @param message 消息内容
     * @return 风险等级: 1-低, 2-中, 3-高
     */
    private Integer assessRiskLevel(AuditRequestMessage message) {
        String preview = message.getContentPreview();
        String title = message.getContentTitle();

        // 模拟关键词检测
        String[] sensitiveWords = {"违规", "不良", "敏感", "违禁"};
        String content = (title != null ? title : "") + (preview != null ? preview : "");

        for (String word : sensitiveWords) {
            if (content.contains(word)) {
                log.info("检测到敏感词[{}], 标记为高风险: contentType={}, contentId={}",
                        word, message.getContentType(), message.getContentId());
                return 3; // 高风险
            }
        }

        // 默认低风险
        return 1;
    }
}

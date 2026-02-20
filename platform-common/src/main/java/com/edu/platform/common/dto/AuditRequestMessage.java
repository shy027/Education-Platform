package com.edu.platform.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 审核请求消息 - 通过RabbitMQ传递给audit-service
 *
 * @author Education Platform
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRequestMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 内容类型: COURSEWARE / POST / COMMENT
     */
    private String contentType;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 内容标题(用于展示)
     */
    private String contentTitle;

    /**
     * 内容预览文本(用于AI审核)
     */
    private String contentPreview;

    /**
     * 初始审核结果: 0-待审核(需人工), 1-默认通过
     * 课件=0, 帖子/评论=1
     */
    private Integer initialAuditResult;

    /**
     * 审核原因(初始通过时使用，如"默认通过")
     */
    private String auditReason;
}

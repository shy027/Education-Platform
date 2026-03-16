package com.edu.platform.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 行为日志上报DTO (跨服务通用)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorLogDTO implements Serializable {
    
    /** 用户ID (可选, 默认取当前登录人) */
    private Long userId;

    /** 课程ID (可选, 若行为关联课程) */
    private Long courseId;

    /** 行为类型: WATCH_VIDEO, READ_DOC, SUBMIT_TASK, POST_COMMENT, ESSENCE_POST等 */
    private String behaviorType;

    /** 行为对象ID (如视频ID, 作业ID, 帖子ID等) */
    private Long behaviorObjectId;

    /** 行为数据(JSON), 可包含性能指标如 { "score": 90, "total": 100 } */
    private String behaviorData;

    /** 持续时间(秒) */
    private Integer durationSeconds;
}

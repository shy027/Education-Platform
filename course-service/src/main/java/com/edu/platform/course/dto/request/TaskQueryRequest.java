package com.edu.platform.course.dto.request;

import com.edu.platform.common.dto.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskQueryRequest extends PageRequest {

    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 关键词（标题）
     */
    private String keyword;
    
    /**
     * 任务类型 (1作业 2测验 3考试)
     */
    private Integer taskType;
    
    /**
     * 状态 (0草稿 1发布 2关闭)
     */
    private Integer status;
}

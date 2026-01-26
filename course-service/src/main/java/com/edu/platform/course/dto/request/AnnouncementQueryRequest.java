package com.edu.platform.course.dto.request;

import com.edu.platform.common.dto.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnnouncementQueryRequest extends PageRequest {

    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 关键词（标题）
     */
    private String keyword;
    
    /**
     * 是否只查询置顶
     */
    private Integer isTop;
}

package com.edu.platform.course.dto.request;

import com.edu.platform.common.dto.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseQueryRequest extends PageRequest {


    /**
     * 关键词(课程名称/编码)
     */
    private String keyword;
    
    private Long schoolId;
    
    private String subjectArea;
    
    /**
     * 加入方式
     */
    private Integer joinType;
    
    /**
     * 状态:0关闭,1开放,2归档
     */
    private Integer status;
    
    /**
     * 审核状态:0待审核,1通过,2拒绝
     */
    private Integer auditStatus;
    
    /**
     * 教师ID (用于查询我的课程)
     */
    private Long teacherId;
}

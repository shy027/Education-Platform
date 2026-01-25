package com.edu.platform.course.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 课程审核请求
 */
@Data
public class CourseAuditRequest {

    /**
     * 是否通过审核
     */
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;
    
    /**
     * 审核备注
     */
    private String auditRemark;
}

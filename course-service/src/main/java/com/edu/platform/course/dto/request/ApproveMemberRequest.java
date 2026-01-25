package com.edu.platform.course.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批成员请求
 */
@Data
public class ApproveMemberRequest {

    /**
     * 是否通过
     */
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;
    
    /**
     * 备注
     */
    private String remark;
}

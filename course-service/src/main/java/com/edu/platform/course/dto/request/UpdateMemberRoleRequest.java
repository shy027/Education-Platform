package com.edu.platform.course.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改成员角色请求
 */
@Data
public class UpdateMemberRoleRequest {

    /**
     * 成员角色: 1=主讲教师, 2=助教, 3=学生
     */
    @NotNull(message = "成员角色不能为空")
    private Integer memberRole;
}

package com.edu.platform.course.dto.request;

import lombok.Data;

/**
 * 成员查询请求
 */
import com.edu.platform.common.dto.request.PageRequest;
import lombok.EqualsAndHashCode;

/**
 * 成员查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberQueryRequest extends PageRequest {

    /**
     * 角色:1主讲教师,2助教,3学生
     */
    private Integer memberRole;
    
    /**
     * 状态:0待审批,1已加入,2已拒绝
     */
    /**
     * 状态:0待审批,1已加入,2已拒绝
     */
    private Integer joinStatus;
}

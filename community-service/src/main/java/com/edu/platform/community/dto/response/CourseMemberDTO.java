package com.edu.platform.community.dto.response;

import lombok.Data;

/**
 * 课程成员DTO
 *
 * @author Education Platform
 */
@Data
public class CourseMemberDTO {
    
    private Long userId;
    
    private Long courseId;
    
    /**
     * 成员角色:1主讲教师,2助教,3学生
     */
    private Integer memberRole;
    
    /**
     * 加入状态:0待审批,1已加入,2已拒绝,3已退出
     */
    private Integer joinStatus;
}

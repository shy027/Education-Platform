package com.edu.platform.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入学校请求
 *
 * @author Education Platform
 */
@Data
public class JoinSchoolRequest {
    
    /**
     * 成员类型 (1:校领导 2:教师 3:学生)
     */
    @NotNull(message = "成员类型不能为空")
    private Integer memberType;
    
    /**
     * 院系/部门
     */
    private String department;
    
    /**
     * 工号/学号
     */
    private String jobNumber;
    
}

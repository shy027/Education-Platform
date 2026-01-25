package com.edu.platform.course.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程成员响应
 */
@Data
public class MemberResponse {

    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String realName;
    
    private String avatar;
    
    private Integer memberRole;
    
    private Integer joinStatus;
    
    private LocalDateTime joinTime;
}

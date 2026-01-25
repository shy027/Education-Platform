package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课程成员表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_member")
public class CourseMember extends BaseEntity {

    @com.baomidou.mybatisplus.annotation.TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;


    private Long courseId;
    
    private Long userId;
    
    /**
     * 角色:1主讲教师,2助教,3学生
     */
    private Integer memberRole;
    
    /**
     * 状态:0待审批,1已加入,2已拒绝,3已退出
     */
    private Integer joinStatus;
    
    private LocalDateTime joinTime;
    
    private LocalDateTime approveTime;
    
    private Long approverId;
}

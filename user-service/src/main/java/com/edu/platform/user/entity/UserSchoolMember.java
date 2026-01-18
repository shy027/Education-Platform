package com.edu.platform.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 学校成员表
 *
 * @author Education Platform
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_school_member")
public class UserSchoolMember extends BaseEntity {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学校ID
     */
    private Long schoolId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 成员类型 (1:校领导 2:教师 3:学生)
     */
    private Integer memberType;
    
    /**
     * 院系/部门
     */
    private String department;
    
    /**
     * 工号/学号
     */
    private String jobNumber;
    
    /**
     * 加入时间
     */
    private LocalDateTime joinTime;
    
    /**
     * 状态 (0:离职/毕业 1:在职/在读)
     */
    private Integer status;
    
}

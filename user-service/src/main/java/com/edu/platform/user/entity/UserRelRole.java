package com.edu.platform.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户角色关联表
 *
 * @author Education Platform
 */
@Data
@TableName("user_rel_role")
public class UserRelRole {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 学校ID(角色范围)
     */
    private Long schoolId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
}

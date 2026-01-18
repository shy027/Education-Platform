package com.edu.platform.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户表
 *
 * @author Education Platform
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_account")
public class UserAccount extends BaseEntity {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名(唯一)
     */
    private String username;
    
    /**
     * 密码(BCrypt加密)
     */
    private String password;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 性别 (0:未知 1:男 2:女)
     */
    private Integer gender;
    
    /**
     * 状态 (0:禁用 1:正常)
     */
    private Integer status;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    
}

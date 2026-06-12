package com.edu.platform.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像链接
     */
    private String avatarUrl;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别 (0:未知 1:男 2:女)
     */
    private Integer gender;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 学号/工号
     */
    private String studentNo;

    /**
     * 院系
     */
    private String department;

    /**
     * 班级
     */
    private String className;
}

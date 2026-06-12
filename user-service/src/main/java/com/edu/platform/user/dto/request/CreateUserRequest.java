package com.edu.platform.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 单独创建用户请求DTO
 *
 * @author Education Platform
 */
@Data
@Schema(description = "单独创建用户请求")
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名（登录账号）", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "密码（不填则默认123456）", example = "123456")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", example = "13800138001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "性别（男/女）", example = "男")
    private String gender;

    @NotBlank(message = "角色不能为空")
    @Schema(description = "角色代码：STUDENT-学生，TEACHER-教师，SCHOOL_LEADER-校领导，ADMIN-管理员",
            example = "STUDENT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleCode;

    @Schema(description = "学号/工号", example = "2024001")
    private String studentNo;

    @Schema(description = "学校名称（管理员填写，校领导默认使用所属学校）", example = "测试大学")
    private String schoolName;

    @Schema(description = "院系", example = "计算机学院")
    private String department;

    @Schema(description = "班级", example = "软件2211")
    private String className;

    @Schema(description = "专业", example = "软件工程")
    private String major;

}

package com.edu.platform.common.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * 仅需要管理员权限
 * 用于系统级别的操作（如角色管理、权限配置等）
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('ROLE_ADMIN')")
public @interface RequireAdmin {
    /**
     * 描述信息
     */
    String value() default "";
}

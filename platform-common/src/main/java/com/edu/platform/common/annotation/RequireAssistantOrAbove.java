package com.edu.platform.common.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * 需要助教或更高级别权限
 * 包含：助教、教师、校领导、管理员
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAnyRole('ROLE_ASSISTANT', 'ROLE_TEACHER', 'ROLE_SCHOOL_LEADER', 'ROLE_ADMIN')")
public @interface RequireAssistantOrAbove {
    /**
     * 描述信息
     */
    String value() default "";
}

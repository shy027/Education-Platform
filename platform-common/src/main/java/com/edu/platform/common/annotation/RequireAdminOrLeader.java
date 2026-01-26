package com.edu.platform.common.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * 需要管理员或校领导权限
 * 用于学校级别的管理操作
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SCHOOL_LEADER')")
public @interface RequireAdminOrLeader {
    /**
     * 描述信息
     */
    String value() default "";
}

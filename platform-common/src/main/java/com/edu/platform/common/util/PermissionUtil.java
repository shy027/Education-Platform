package com.edu.platform.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 通用权限工具类
 * 用于统一管理跨服务的权限检查逻辑
 */
public class PermissionUtil {

    /**
     * 检查是否是管理员或校领导
     * 这两个角色拥有学校级别的管理权限
     */
    public static boolean isAdminOrLeader() {
        return hasAnyRole("ROLE_ADMIN", "ROLE_SCHOOL_LEADER");
    }

    /**
     * 检查是否是教师或更高级别
     * 包含：教师、校领导、管理员
     */
    public static boolean isTeacherOrAbove() {
        return hasAnyRole("ROLE_TEACHER", "ROLE_SCHOOL_LEADER", "ROLE_ADMIN");
    }

    /**
     * 检查是否是助教或更高级别
     * 包含：助教、教师、校领导、管理员
     */
    public static boolean isAssistantOrAbove() {
        return hasAnyRole("ROLE_ASSISTANT", "ROLE_TEACHER", "ROLE_SCHOOL_LEADER", "ROLE_ADMIN");
    }

    /**
     * 检查是否只有管理员权限
     * 某些系统级操作只能由管理员执行
     */
    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    /**
     * 检查是否有指定角色
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否有任意一个角色
     */
    public static boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        for (String role : roles) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        
        // 如果是自定义的 UserDetails，需要根据实际情况获取
        return null;
    }
}

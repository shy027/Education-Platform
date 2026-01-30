package com.edu.platform.common.utils;

import java.util.List;

/**
 * 用户上下文工具类
 * 用于在当前线程中存储和获取用户信息
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> ROLES_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 设置当前用户名
     */
    public static void setUsername(String username) {
        USERNAME_HOLDER.set(username);
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        return USERNAME_HOLDER.get();
    }

    /**
     * 设置当前用户角色列表
     */
    public static void setRoles(List<String> roles) {
        ROLES_HOLDER.set(roles);
    }

    /**
     * 获取当前用户角色列表
     */
    public static List<String> getRoles() {
        return ROLES_HOLDER.get();
    }

    /**
     * 检查当前用户是否拥有指定角色
     */
    public static boolean hasRole(String role) {
        List<String> roles = ROLES_HOLDER.get();
        return roles != null && roles.contains(role);
    }

    /**
     * 清除当前线程的用户信息
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        USERNAME_HOLDER.remove();
        ROLES_HOLDER.remove();
    }
}

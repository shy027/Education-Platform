package com.edu.platform.course.context;

import java.util.Collections;
import java.util.List;

/**
 * 用户上下文
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> ROLES = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void setRoles(List<String> roles) {
        ROLES.set(roles);
    }

    public static List<String> getRoles() {
        return ROLES.get() != null ? ROLES.get() : Collections.emptyList();
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        ROLES.remove();
    }
    
    /**
     * 是否包含指定角色
     */
    public static boolean hasRole(String role) {
        List<String> roles = getRoles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        // 支持 "TEACHER" 或 "ROLE_TEACHER" 格式
        return roles.contains(role) || roles.contains("ROLE_" + role);
    }
}

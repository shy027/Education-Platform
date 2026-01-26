package com.edu.platform.course.util;

import com.edu.platform.course.context.UserContext;

/**
 * 权限工具类
 * 统一管理课程服务的权限判断逻辑
 */
public class PermissionUtil {

    /**
     * 检查是否是管理员或校领导
     * 管理员和校领导拥有最高权限
     */
    public static boolean isAdminOrLeader() {
        return UserContext.hasRole("ADMIN") || UserContext.hasRole("SCHOOL_LEADER");
    }

    /**
     * 检查是否是教师、校领导或管理员
     * 校领导具有教师的所有权限
     */
    public static boolean isTeacherOrAbove() {
        return UserContext.hasRole("TEACHER") 
            || UserContext.hasRole("SCHOOL_LEADER") 
            || UserContext.hasRole("ADMIN");
    }

    /**
     * 检查是否是助教、教师、校领导或管理员
     */
    public static boolean isAssistantOrAbove() {
        return UserContext.hasRole("ASSISTANT") 
            || UserContext.hasRole("TEACHER") 
            || UserContext.hasRole("SCHOOL_LEADER") 
            || UserContext.hasRole("ADMIN");
    }

    /**
     * 检查是否有课程管理权限
     * 课程教师、校领导或管理员可以管理课程
     * 
     * @param courseTeacherId 课程教师ID
     * @param currentUserId 当前用户ID
     * @return 是否有权限
     */
    public static boolean hasCourseManagePermission(Long courseTeacherId, Long currentUserId) {
        // 管理员或校领导有权限
        if (isAdminOrLeader()) {
            return true;
        }
        // 课程教师有权限
        return courseTeacherId != null && courseTeacherId.equals(currentUserId);
    }
}

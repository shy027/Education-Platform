package com.edu.platform.course.aspect;

import com.edu.platform.common.utils.UserContext;
import com.edu.platform.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 管理员权限检查切面
 */
@Slf4j
@Aspect
@Component
public class AdminPermissionAspect {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    /**
     * 检查是否有管理员权限
     */
    @Before("@annotation(com.edu.platform.course.annotation.RequireAdmin)")
    public void checkAdminPermission(JoinPoint joinPoint) {
        // 获取当前用户角色
        List<String> roles = UserContext.getRoles();
        
        if (roles == null || roles.isEmpty()) {
            log.warn("用户未登录或无角色信息, userId={}", UserContext.getUserId());
            throw new BusinessException("无权限访问,需要管理员权限");
        }

        // 检查是否有管理员角色
        boolean isAdmin = roles.contains(ADMIN_ROLE) || roles.contains(SUPER_ADMIN_ROLE);
        
        if (!isAdmin) {
            log.warn("用户无管理员权限, userId={}, roles={}, method={}", 
                    UserContext.getUserId(), 
                    roles, 
                    joinPoint.getSignature().getName());
            throw new BusinessException("无权限访问,需要管理员权限");
        }

        log.debug("管理员权限检查通过, userId={}, roles={}", UserContext.getUserId(), roles);
    }
}

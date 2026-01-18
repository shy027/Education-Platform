package com.edu.platform.user.interceptor;

import com.edu.platform.common.constant.Constants;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 *
 * @author Education Platform
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取Token
        String token = request.getHeader(Constants.AUTHORIZATION);
        
        if (token == null || !token.startsWith(Constants.TOKEN_PREFIX)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage());
        }
        
        // 去除Bearer前缀
        token = token.substring(Constants.TOKEN_PREFIX.length());
        
        // 验证Token
        if (!JwtUtil.validateToken(token)) {
            if (JwtUtil.isTokenExpired(token)) {
                throw new BusinessException(ResultCode.TOKEN_EXPIRED.getCode(), ResultCode.TOKEN_EXPIRED.getMessage());
            }
            throw new BusinessException(ResultCode.TOKEN_INVALID.getCode(), ResultCode.TOKEN_INVALID.getMessage());
        }
        
        // 获取用户ID并存入请求属性
        Long userId = JwtUtil.getUserId(token);
        request.setAttribute(Constants.USER_ID, userId);
        request.setAttribute(Constants.USERNAME, JwtUtil.getUsername(token));
        
        return true;
    }
    
}

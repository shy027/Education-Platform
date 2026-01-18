package com.edu.platform.user.service;

import com.edu.platform.user.dto.request.*;
import com.edu.platform.user.dto.response.CurrentUserResponse;
import com.edu.platform.user.dto.response.LoginResponse;

/**
 * 认证服务接口
 *
 * @author Education Platform
 */
public interface AuthService {
    
    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 用户ID
     */
    Long register(RegisterRequest request);
    
    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应(包含token和用户信息)
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    CurrentUserResponse getCurrentUser(Long userId);
    
    /**
     * 修改个人信息
     *
     * @param userId 用户ID
     * @param request 修改请求
     */
    void updateProfile(Long userId, UpdateProfileRequest request);
    
    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param request 修改密码请求
     */
    void updatePassword(Long userId, UpdatePasswordRequest request);
    
    /**
     * 手机号密码登录
     *
     * @param request 手机号密码登录请求
     * @return 登录响应(包含token和用户信息)
     */
    LoginResponse phonePasswordLogin(PhonePasswordLoginRequest request);
    
    /**
     * 手机号验证码登录
     *
     * @param request 手机号验证码登录请求
     * @return 登录响应(包含token和用户信息)
     */
    LoginResponse phoneCodeLogin(PhoneCodeLoginRequest request);
    
}

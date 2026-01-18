package com.edu.platform.user.controller;

import com.edu.platform.common.constant.Constants;
import com.edu.platform.common.result.Result;
import com.edu.platform.user.dto.request.*;
import com.edu.platform.user.dto.response.CurrentUserResponse;
import com.edu.platform.user.dto.response.LoginResponse;
import com.edu.platform.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 *
 * @author Education Platform
 */
@Tag(name = "用户认证", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", request.getUsername());
        
        return Result.success("注册成功", data);
    }
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }
    
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current-user")
    public Result<CurrentUserResponse> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.USER_ID);
        CurrentUserResponse response = authService.getCurrentUser(userId);
        return Result.success(response);
    }
    
    @Operation(summary = "修改个人信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody UpdateProfileRequest updateRequest) {
        Long userId = (Long) request.getAttribute(Constants.USER_ID);
        authService.updateProfile(userId, updateRequest);
        return Result.success("修改成功", null);
    }
    
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(
            HttpServletRequest request,
            @Valid @RequestBody UpdatePasswordRequest updateRequest) {
        Long userId = (Long) request.getAttribute(Constants.USER_ID);
        authService.updatePassword(userId, updateRequest);
        return Result.success("密码修改成功", null);
    }
    
}

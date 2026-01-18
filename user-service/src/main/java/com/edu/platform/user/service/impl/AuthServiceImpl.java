package com.edu.platform.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.common.utils.JwtUtil;
import com.edu.platform.common.utils.PasswordUtil;
import com.edu.platform.user.dto.request.*;
import com.edu.platform.user.dto.response.CurrentUserResponse;
import com.edu.platform.user.dto.response.LoginResponse;
import com.edu.platform.user.entity.*;
import com.edu.platform.user.mapper.*;
import com.edu.platform.user.service.AuthService;
import com.edu.platform.user.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserAccountMapper userAccountMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserRelRoleMapper userRelRoleMapper;
    private final UserSchoolMapper userSchoolMapper;
    private final UserSchoolMemberMapper userSchoolMemberMapper;
    private final SmsService smsService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getUsername, request.getUsername());
        if (userAccountMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), ResultCode.USER_ALREADY_EXISTS.getMessage());
        }
        
        // 检查邮箱是否已被使用
        if (request.getEmail() != null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserAccount::getEmail, request.getEmail());
            if (userAccountMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("邮箱已被使用");
            }
        }
        
        // 创建用户
        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setStatus(1); // 默认正常状态
        user.setGender(0); // 默认未知
        
        userAccountMapper.insert(user);
        
        // 分配默认角色(学生)
        LambdaQueryWrapper<UserRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(UserRole::getRoleCode, "STUDENT");
        UserRole studentRole = userRoleMapper.selectOne(roleWrapper);
        
        if (studentRole != null) {
            UserRelRole userRelRole = new UserRelRole();
            userRelRole.setUserId(user.getId());
            userRelRole.setRoleId(studentRole.getId());
            userRelRole.setCreatedTime(LocalDateTime.now());
            userRelRoleMapper.insert(userRelRole);
        }
        
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
        return user.getId();
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getUsername, request.getUsername());
        UserAccount user = userAccountMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR.getCode(), ResultCode.USERNAME_OR_PASSWORD_ERROR.getMessage());
        }
        
        // 验证密码
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR.getCode(), ResultCode.USERNAME_OR_PASSWORD_ERROR.getMessage());
        }
        
        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账号已被禁用");
        }
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userAccountMapper.updateById(user);
        
        // 获取用户角色
        List<String> roles = getUserRoles(user.getId());
        
        // 生成Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setAvatar(user.getAvatarUrl());
        userInfo.setRoles(roles);
        
        response.setUserInfo(userInfo);
        
        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return response;
    }
    
    @Override
    public CurrentUserResponse getCurrentUser(Long userId) {
        // 查询用户信息
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), ResultCode.USER_NOT_FOUND.getMessage());
        }
        
        // 获取用户角色
        List<String> roles = getUserRoles(userId);
        
        // 获取用户学校
        List<CurrentUserResponse.SchoolInfo> schools = getUserSchools(userId);
        
        // 构建响应
        CurrentUserResponse response = new CurrentUserResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAvatar(user.getAvatarUrl());
        response.setGender(user.getGender());
        response.setRoles(roles);
        response.setSchools(schools);
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), ResultCode.USER_NOT_FOUND.getMessage());
        }
        
        // 检查邮箱是否被其他用户使用
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserAccount::getEmail, request.getEmail());
            wrapper.ne(UserAccount::getId, userId);
            if (userAccountMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("邮箱已被使用");
            }
        }
        
        // 检查手机号是否被其他用户使用
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserAccount::getPhone, request.getPhone());
            wrapper.ne(UserAccount::getId, userId);
            if (userAccountMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("手机号已被使用");
            }
        }
        
        // 更新用户信息
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatarUrl(request.getAvatar());
        user.setGender(request.getGender());
        
        userAccountMapper.updateById(user);
        log.info("用户信息更新成功: userId={}", userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), ResultCode.USER_NOT_FOUND.getMessage());
        }
        
        // 验证旧密码
        if (!PasswordUtil.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        
        // 更新密码
        user.setPassword(PasswordUtil.encode(request.getNewPassword()));
        userAccountMapper.updateById(user);
        
        log.info("用户密码修改成功: userId={}", userId);
    }
    
    /**
     * 获取用户角色列表
     */
    private List<String> getUserRoles(Long userId) {
        // 查询用户角色关联
        LambdaQueryWrapper<UserRelRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelRole::getUserId, userId);
        List<UserRelRole> userRelRoles = userRelRoleMapper.selectList(wrapper);
        
        if (userRelRoles.isEmpty()) {
            return List.of();
        }
        
        // 查询角色信息
        List<Long> roleIds = userRelRoles.stream()
                .map(UserRelRole::getRoleId)
                .collect(Collectors.toList());
        
        List<UserRole> roles = userRoleMapper.selectBatchIds(roleIds);
        
        return roles.stream()
                .map(UserRole::getRoleCode)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户学校列表
     */
    private List<CurrentUserResponse.SchoolInfo> getUserSchools(Long userId) {
        // 查询用户学校成员关联
        LambdaQueryWrapper<UserSchoolMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSchoolMember::getUserId, userId);
        wrapper.eq(UserSchoolMember::getStatus, 1); // 在读/在职
        List<UserSchoolMember> members = userSchoolMemberMapper.selectList(wrapper);
        
        if (members.isEmpty()) {
            return List.of();
        }
        
        // 查询学校信息
        List<Long> schoolIds = members.stream()
                .map(UserSchoolMember::getSchoolId)
                .collect(Collectors.toList());
        
        List<UserSchool> schools = userSchoolMapper.selectBatchIds(schoolIds);
        
        // 构建响应
        return members.stream().map(member -> {
            UserSchool school = schools.stream()
                    .filter(s -> s.getId().equals(member.getSchoolId()))
                    .findFirst()
                    .orElse(null);
            
            if (school == null) {
                return null;
            }
            
            CurrentUserResponse.SchoolInfo schoolInfo = new CurrentUserResponse.SchoolInfo();
            schoolInfo.setSchoolId(school.getId());
            schoolInfo.setSchoolName(school.getSchoolName());
            schoolInfo.setMemberType(member.getMemberType());
            return schoolInfo;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }
    
    @Override
    public LoginResponse phonePasswordLogin(PhonePasswordLoginRequest request) {
        // 根据手机号查询用户
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getPhone, request.getPhone());
        wrapper.eq(UserAccount::getStatus, 1);
        UserAccount user = userAccountMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "手机号未注册");
        }
        
        // 验证密码
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "密码错误");
        }
        
        return buildLoginResponse(user);
    }
    
    @Override
    public LoginResponse phoneCodeLogin(PhoneCodeLoginRequest request) {
        // 验证验证码
        if (!smsService.verifyCode(request.getPhone(), request.getCode())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "验证码错误或已过期");
        }
        
        // 根据手机号查询用户
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getPhone, request.getPhone());
        wrapper.eq(UserAccount::getStatus, 1);
        UserAccount user = userAccountMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "手机号未注册");
        }
        
        return buildLoginResponse(user);
    }
    
    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(UserAccount user) {
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userAccountMapper.updateById(user);
        
        // 生成token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setAvatar(user.getAvatarUrl());
        response.setUserInfo(userInfo);
        
        return response;
    }
    
}

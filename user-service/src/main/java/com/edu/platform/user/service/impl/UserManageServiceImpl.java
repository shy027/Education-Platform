package com.edu.platform.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.constant.Constants;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.common.utils.PasswordUtil;
import com.edu.platform.user.dto.request.UserQueryRequest;
import com.edu.platform.user.dto.response.UserManageResponse;
import com.edu.platform.user.entity.UserAccount;
import com.edu.platform.user.entity.UserRelRole;
import com.edu.platform.user.entity.UserRole;
import com.edu.platform.user.mapper.UserAccountMapper;
import com.edu.platform.user.mapper.UserRelRoleMapper;
import com.edu.platform.user.mapper.UserRoleMapper;
import com.edu.platform.user.service.UserManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManageServiceImpl implements UserManageService {
    
    private final UserAccountMapper userAccountMapper;
    private final UserRelRoleMapper userRelRoleMapper;
    private final UserRoleMapper userRoleMapper;
    
    @Override
    public PageResult<UserManageResponse> getUserList(UserQueryRequest request) {
        // 参数校验
        if (request.getPageNum() == null || request.getPageNum() < 1) {
            request.setPageNum(Constants.DEFAULT_PAGE_NUM);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(Constants.DEFAULT_PAGE_SIZE);
        }
        if (request.getPageSize() > Constants.MAX_PAGE_SIZE) {
            request.setPageSize(Constants.MAX_PAGE_SIZE);
        }
        
        // 构建查询条件
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(request.getUsername())) {
            wrapper.like(UserAccount::getUsername, request.getUsername());
        }
        
        if (StrUtil.isNotBlank(request.getRealName())) {
            wrapper.like(UserAccount::getRealName, request.getRealName());
        }
        
        if (StrUtil.isNotBlank(request.getPhone())) {
            wrapper.eq(UserAccount::getPhone, request.getPhone());
        }
        
        if (StrUtil.isNotBlank(request.getEmail())) {
            wrapper.like(UserAccount::getEmail, request.getEmail());
        }
        
        if (request.getStatus() != null) {
            wrapper.eq(UserAccount::getStatus, request.getStatus());
        }
        
        wrapper.orderByDesc(UserAccount::getCreatedTime);
        
        // 分页查询
        Page<UserAccount> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<UserAccount> result = userAccountMapper.selectPage(page, wrapper);
        
        // 转换为响应对象
        List<UserManageResponse> list = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        // 如果指定了角色过滤,需要二次过滤
        if (request.getRoleId() != null) {
            list = list.stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> role.getRoleId().equals(request.getRoleId())))
                    .collect(Collectors.toList());
        }
        
        return PageResult.of(result.getTotal(), list);
    }
    
    @Override
    public UserManageResponse getUserDetail(Long userId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        return convertToResponse(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        
        user.setStatus(status);
        userAccountMapper.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resetPassword(Long userId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        
        // 重置为默认密码123456
        String newPassword = "123456";
        
        // 加密并更新
        user.setPassword(PasswordUtil.encode(newPassword));
        userAccountMapper.updateById(user);
        
        return newPassword;
    }
    
    /**
     * 转换为响应对象
     */
    private UserManageResponse convertToResponse(UserAccount user) {
        UserManageResponse response = new UserManageResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setGender(user.getGender());
        response.setStatus(user.getStatus());
        response.setLastLoginTime(user.getLastLoginTime());
        response.setCreatedTime(user.getCreatedTime());
        
        // 查询用户角色
        response.setRoles(getUserRoles(user.getId()));
        
        return response;
    }
    
    /**
     * 获取用户角色列表
     */
    private List<UserManageResponse.RoleInfo> getUserRoles(Long userId) {
        // 查询用户角色关联
        LambdaQueryWrapper<UserRelRole> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(UserRelRole::getUserId, userId);
        List<UserRelRole> userRoles = userRelRoleMapper.selectList(relWrapper);
        
        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询角色信息
        List<Long> roleIds = userRoles.stream()
                .map(UserRelRole::getRoleId)
                .collect(Collectors.toList());
        
        List<UserRole> roles = userRoleMapper.selectBatchIds(roleIds);
        
        // 转换为RoleInfo
        return roles.stream().map(role -> {
            UserManageResponse.RoleInfo roleInfo = new UserManageResponse.RoleInfo();
            roleInfo.setRoleId(role.getId());
            roleInfo.setRoleName(role.getRoleName());
            roleInfo.setRoleCode(role.getRoleCode());
            return roleInfo;
        }).collect(Collectors.toList());
    }
    
}

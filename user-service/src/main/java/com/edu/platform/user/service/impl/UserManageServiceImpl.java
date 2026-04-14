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
import com.edu.platform.user.entity.UserSchoolMember;
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
    private final com.edu.platform.user.mapper.UserSchoolMemberMapper userSchoolMemberMapper;
    private final com.edu.platform.user.mapper.UserSchoolMapper userSchoolMapper;
    
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
        
        // 增加学校/院系/班级过滤
        if (request.getSchoolId() != null || StrUtil.isNotBlank(request.getDepartment()) || StrUtil.isNotBlank(request.getClassName())) {
            StringBuilder subQuery = new StringBuilder("SELECT user_id FROM user_school_member WHERE 1=1");
            if (request.getSchoolId() != null) {
                subQuery.append(" AND school_id = ").append(request.getSchoolId());
            }
            if (StrUtil.isNotBlank(request.getDepartment())) {
                subQuery.append(" AND department = '").append(request.getDepartment()).append("'");
            }
            if (StrUtil.isNotBlank(request.getClassName())) {
                subQuery.append(" AND class_name = '").append(request.getClassName()).append("'");
            }
            wrapper.inSql(UserAccount::getId, subQuery.toString());
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
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setGender(user.getGender());
        response.setStatus(user.getStatus());
        response.setLastLoginTime(user.getLastLoginTime());
        response.setCreatedTime(user.getCreatedTime());
        
        // 查询学校成员信息
        LambdaQueryWrapper<UserSchoolMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(UserSchoolMember::getUserId, user.getId())
                     .orderByDesc(UserSchoolMember::getJoinTime)
                     .last("LIMIT 1");
        UserSchoolMember member = userSchoolMemberMapper.selectOne(memberWrapper);
        if (member != null) {
            response.setSchoolId(member.getSchoolId());
            response.setDepartment(member.getDepartment());
            response.setClassName(member.getClassName());
            response.setStudentNo(member.getJobNumber());
            
            // 查询学校名称
            com.edu.platform.user.entity.UserSchool school = userSchoolMapper.selectById(member.getSchoolId());
            if (school != null) {
                response.setSchoolName(school.getSchoolName());
            }
        }
        
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
    
    @Override
    public java.util.Map<Long, UserManageResponse> batchGetUserInfo(java.util.List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        
        // 批量查询用户
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserAccount::getId, userIds);
        List<UserAccount> users = userAccountMapper.selectList(wrapper);
        
        // 转换为Map
        return users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toMap(UserManageResponse::getUserId, user -> user));
    }

    @Override
    public java.util.Map<String, Object> getUserStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // 总用户数
        Long totalUsers = userAccountMapper.selectCount(new LambdaQueryWrapper<>());
        stats.put("totalUsers", totalUsers);
        
        // 今日增长
        java.time.LocalDateTime todayStart = java.time.LocalDateTime.now().with(java.time.LocalTime.MIN);
        Long todayGrowth = userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>()
                .ge(UserAccount::getCreatedTime, todayStart));
        stats.put("todayGrowth", todayGrowth);
        
        return stats;
    }
    @Override
    public Long getUserSchoolId(Long userId) {
        LambdaQueryWrapper<UserSchoolMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSchoolMember::getUserId, userId)
               .orderByDesc(UserSchoolMember::getJoinTime)
               .last("LIMIT 1");
        UserSchoolMember member = userSchoolMemberMapper.selectOne(wrapper);
        return member != null ? member.getSchoolId() : null;
    }

    @Override
    public java.util.List<Long> queryUserIds(String department, String className) {
        if (StrUtil.isBlank(department) && StrUtil.isBlank(className)) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<UserSchoolMember> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(department)) {
            wrapper.eq(UserSchoolMember::getDepartment, department);
        }
        if (StrUtil.isNotBlank(className)) {
            wrapper.eq(UserSchoolMember::getClassName, className);
        }
        
        List<UserSchoolMember> members = userSchoolMemberMapper.selectList(wrapper);
        return members.stream().map(UserSchoolMember::getUserId).distinct().collect(Collectors.toList());
    }

    @Override
    public java.util.Map<String, java.util.List<String>> getMemberFilterOptions(java.util.List<Long> userIds) {
        java.util.Map<String, java.util.List<String>> result = new java.util.HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            result.put("departments", new ArrayList<>());
            result.put("classNames", new ArrayList<>());
            return result;
        }
        
        LambdaQueryWrapper<UserSchoolMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserSchoolMember::getUserId, userIds);
        List<UserSchoolMember> members = userSchoolMemberMapper.selectList(wrapper);
        
        List<String> departments = members.stream()
                .map(UserSchoolMember::getDepartment)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        List<String> classNames = members.stream()
                .map(UserSchoolMember::getClassName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        result.put("departments", departments);
        result.put("classNames", classNames);
        return result;
    }
}

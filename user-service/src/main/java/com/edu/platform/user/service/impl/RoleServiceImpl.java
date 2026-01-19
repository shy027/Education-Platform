package com.edu.platform.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.constant.Constants;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.user.dto.request.RoleQueryRequest;
import com.edu.platform.user.dto.response.RoleResponse;
import com.edu.platform.user.entity.UserRole;
import com.edu.platform.user.mapper.UserRoleMapper;
import com.edu.platform.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    
    private final UserRoleMapper userRoleMapper;
    
    @Override
    public PageResult<RoleResponse> getRoleList(RoleQueryRequest request) {
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
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(request.getRoleName())) {
            wrapper.like(UserRole::getRoleName, request.getRoleName());
        }
        
        if (StrUtil.isNotBlank(request.getRoleCode())) {
            wrapper.eq(UserRole::getRoleCode, request.getRoleCode());
        }
        
        if (request.getStatus() != null) {
            wrapper.eq(UserRole::getStatus, request.getStatus());
        }
        
        wrapper.orderByAsc(UserRole::getSortOrder)
               .orderByDesc(UserRole::getCreatedTime);
        
        // 分页查询
        Page<UserRole> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<UserRole> result = userRoleMapper.selectPage(page, wrapper);
        
        // 转换为响应对象
        List<RoleResponse> list = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(result.getTotal(), list);
    }
    
    @Override
    public RoleResponse getRoleDetail(Long roleId) {
        UserRole role = userRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "角色不存在");
        }
        return convertToResponse(role);
    }
    
    @Override
    public List<RoleResponse> getAllRoles() {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getStatus, 1)
               .orderByAsc(UserRole::getSortOrder);
        
        List<UserRole> roles = userRoleMapper.selectList(wrapper);
        return roles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为响应对象
     */
    private RoleResponse convertToResponse(UserRole role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setRoleName(role.getRoleName());
        response.setRoleCode(role.getRoleCode());
        response.setDescription(role.getDescription());
        response.setStatus(role.getStatus());
        response.setSortOrder(role.getSortOrder());
        response.setCreatedTime(role.getCreatedTime());
        return response;
    }
    
}

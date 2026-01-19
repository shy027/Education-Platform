package com.edu.platform.user.service;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.user.dto.request.RoleQueryRequest;
import com.edu.platform.user.dto.response.RoleResponse;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author Education Platform
 */
public interface RoleService {
    
    /**
     * 角色列表查询(分页)
     *
     * @param request 查询请求
     * @return 角色列表
     */
    PageResult<RoleResponse> getRoleList(RoleQueryRequest request);
    
    /**
     * 角色详情查询
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    RoleResponse getRoleDetail(Long roleId);
    
    /**
     * 获取所有角色(不分页)
     *
     * @return 角色列表
     */
    List<RoleResponse> getAllRoles();
    
}

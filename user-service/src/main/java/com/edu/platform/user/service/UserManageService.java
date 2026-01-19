package com.edu.platform.user.service;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.user.dto.request.UserQueryRequest;
import com.edu.platform.user.dto.response.UserManageResponse;

/**
 * 用户管理服务接口
 *
 * @author Education Platform
 */
public interface UserManageService {
    
    /**
     * 用户列表查询(分页)
     *
     * @param request 查询请求
     * @return 用户列表
     */
    PageResult<UserManageResponse> getUserList(UserQueryRequest request);
    
    /**
     * 用户详情查询
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    UserManageResponse getUserDetail(Long userId);
    
    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     */
    void updateUserStatus(Long userId, Integer status);
    
    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @return 新密码
     */
    String resetPassword(Long userId);
    
}

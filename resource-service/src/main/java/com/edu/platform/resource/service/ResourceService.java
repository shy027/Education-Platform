package com.edu.platform.resource.service;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.resource.dto.request.ResourceAuditRequest;
import com.edu.platform.resource.dto.request.ResourceCreateRequest;
import com.edu.platform.resource.dto.request.ResourceQueryRequest;
import com.edu.platform.resource.dto.request.ResourceUpdateRequest;
import com.edu.platform.resource.dto.response.AuditLogResponse;
import com.edu.platform.resource.dto.response.ResourceDetailResponse;
import com.edu.platform.resource.dto.response.ResourceResponse;

import java.util.List;

/**
 * 资源服务接口
 *
 * @author Education Platform
 */
public interface ResourceService {
    
    /**
     * 创建资源
     * 管理员:直接发布(status=2)
     * 教师:保存为草稿(status=0)
     *
     * @param request 创建请求
     * @param userId 用户ID
     * @param userRole 用户角色
     * @return 资源ID
     */
    Long createResource(ResourceCreateRequest request, Long userId, String userRole);
    
    /**
     * 更新资源
     *
     * @param resourceId 资源ID
     * @param request 更新请求
     * @param userId 用户ID
     */
    void updateResource(Long resourceId, ResourceUpdateRequest request, Long userId);
    
    /**
     * 删除资源
     *
     * @param resourceId 资源ID
     * @param userId 用户ID
     */
    void deleteResource(Long resourceId, Long userId);
    
    /**
     * 获取资源详情
     *
     * @param resourceId 资源ID
     * @return 资源详情
     */
    ResourceDetailResponse getResourceDetail(Long resourceId);
    
    /**
     * 分页查询资源列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<ResourceResponse> getResourceList(ResourceQueryRequest request);
    
    /**
     * 提交审核
     * 状态:草稿(0) → 待审核(1)
     *
     * @param resourceId 资源ID
     * @param userId 用户ID
     */
    void submitForAudit(Long resourceId, Long userId);
    
    /**
     * 审核资源
     * 通过:待审核(1) → 已发布(2)
     * 拒绝:待审核(1) → 已拒绝(3)
     *
     * @param resourceId 资源ID
     * @param request 审核请求
     * @param auditorId 审核人ID
     */
    void auditResource(Long resourceId, ResourceAuditRequest request, Long auditorId);
    
    /**
     * 获取待审核列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<ResourceResponse> getPendingList(Integer pageNum, Integer pageSize);
    
    /**
     * 下架资源
     * 状态:已发布(2) → 已下架(4)
     *
     * @param resourceId 资源ID
     * @param userId 用户ID
     */
    void offlineResource(Long resourceId, Long userId);
    
    /**
     * 获取审核历史
     *
     * @param resourceId 资源ID
     * @return 审核记录列表
     */
    List<AuditLogResponse> getAuditLogs(Long resourceId);
    
    /**
     * 增加浏览次数
     *
     * @param resourceId 资源ID
     */
    void incrementViewCount(Long resourceId);
    
}

package com.edu.platform.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.community.dto.request.CreateGroupRequest;
import com.edu.platform.community.dto.request.GroupQueryRequest;
import com.edu.platform.community.dto.request.UpdateGroupRequest;
import com.edu.platform.community.dto.response.GroupDetailResponse;
import com.edu.platform.community.dto.response.GroupListResponse;
import com.edu.platform.community.dto.response.GroupMemberResponse;

/**
 * 小组服务接口
 *
 * @author Education Platform
 */
public interface GroupService {
    
    /**
     * 创建小组(仅教师)
     *
     * @param request 创建请求
     * @param userId 用户ID
     * @return 小组详情
     */
    GroupDetailResponse createGroup(CreateGroupRequest request, Long userId);
    
    /**
     * 更新小组信息
     *
     * @param groupId 小组ID
     * @param request 更新请求
     * @param userId 用户ID
     */
    void updateGroup(Long groupId, UpdateGroupRequest request, Long userId);
    
    /**
     * 解散小组(逻辑删除)
     *
     * @param groupId 小组ID
     * @param userId 用户ID
     */
    void deleteGroup(Long groupId, Long userId);
    
    /**
     * 获取小组详情
     *
     * @param groupId 小组ID
     * @param userId 用户ID
     * @return 小组详情
     */
    GroupDetailResponse getGroupDetail(Long groupId, Long userId);
    
    /**
     * 查询小组列表
     *
     * @param request 查询请求
     * @return 小组列表
     */
    Page<GroupListResponse> listGroups(GroupQueryRequest request);
    
    /**
     * 申请加入小组
     *
     * @param groupId 小组ID
     * @param userId 用户ID
     */
    void applyJoinGroup(Long groupId, Long userId);
    
    /**
     * 审批加入申请(仅教师)
     *
     * @param groupId 小组ID
     * @param memberId 成员记录ID
     * @param approveStatus 审批状态(1同意/2拒绝)
     * @param userId 审批人ID
     */
    void approveJoinRequest(Long groupId, Long memberId, Integer approveStatus, Long userId);
    
    /**
     * 退出小组
     *
     * @param groupId 小组ID
     * @param userId 用户ID
     */
    void quitGroup(Long groupId, Long userId);
    
    /**
     * 查询小组成员列表
     *
     * @param groupId 小组ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 成员列表
     */
    Page<GroupMemberResponse> listGroupMembers(Long groupId, Integer pageNum, Integer pageSize);
    
    /**
     * 查询待审批申请列表(仅教师)
     *
     * @param groupId 小组ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param userId 用户ID
     * @return 待审批列表
     */
    Page<GroupMemberResponse> listJoinRequests(Long groupId, Integer pageNum, Integer pageSize, Long userId);
    
    /**
     * 教师手动添加成员到小组
     *
     * @param groupId 小组ID
     * @param targetUserId 要添加的用户ID
     * @param operatorId 操作人ID(教师)
     */
    void addMemberByTeacher(Long groupId, Long targetUserId, Long operatorId);
    
    /**
     * 教师手动移除小组成员
     *
     * @param groupId 小组ID
     * @param targetUserId 要移除的用户ID
     * @param operatorId 操作人ID(教师)
     */
    void removeMemberByTeacher(Long groupId, Long targetUserId, Long operatorId);
}

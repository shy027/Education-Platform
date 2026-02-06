package com.edu.platform.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.community.client.UserServiceClient;
import com.edu.platform.community.dto.request.CreateGroupRequest;
import com.edu.platform.community.dto.request.GroupQueryRequest;
import com.edu.platform.community.dto.request.UpdateGroupRequest;
import com.edu.platform.community.dto.response.GroupDetailResponse;
import com.edu.platform.community.dto.response.GroupListResponse;
import com.edu.platform.community.dto.response.GroupMemberResponse;
import com.edu.platform.community.dto.response.CourseMemberDTO;
import com.edu.platform.community.dto.response.UserInfoDTO;
import com.edu.platform.community.entity.CommunityGroup;
import com.edu.platform.community.entity.CommunityGroupMember;
import com.edu.platform.community.entity.CommunityPost;
import com.edu.platform.community.mapper.CommunityGroupMapper;
import com.edu.platform.community.mapper.CommunityGroupMemberMapper;
import com.edu.platform.community.mapper.CommunityPostMapper;
import com.edu.platform.community.service.GroupService;
import com.edu.platform.community.util.PermissionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小组服务实现类
 *
 * @author Education Platform
 */
@Slf4j
@Service
public class GroupServiceImpl implements GroupService {
    
    @Autowired
    private CommunityGroupMapper groupMapper;
    
    @Autowired
    private CommunityGroupMemberMapper groupMemberMapper;
    
    @Autowired
    private CommunityPostMapper postMapper;
    
    @Autowired
    private PermissionUtil permissionUtil;
    
    @Autowired(required = false)
    private UserServiceClient userServiceClient;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupDetailResponse createGroup(CreateGroupRequest request, Long userId) {
        log.info("创建小组, userId={}, request={}", userId, request);
        
        // 1. 验证用户是否为教师
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, request.getCourseId());
        if (memberDTO.getMemberRole() != 1 && memberDTO.getMemberRole() != 2) {
            throw new BusinessException("只有教师可以创建小组");
        }
        
        // 2. 创建小组
        CommunityGroup group = new CommunityGroup();
        group.setCourseId(request.getCourseId());
        group.setGroupName(request.getGroupName());
        group.setGroupIntro(request.getGroupIntro());
        group.setCreatorId(userId);
        group.setMaxMembers(request.getMaxMembers());
        group.setMemberCount(1); // 创建者自动加入
        group.setStatus(1);
        group.setCreatedTime(LocalDateTime.now());
        group.setUpdatedTime(LocalDateTime.now());
        group.setIsDeleted(0);
        
        groupMapper.insert(group);
        
        // 4. 自动将创建者加入小组(已审批通过)
        CommunityGroupMember member = new CommunityGroupMember();
        member.setGroupId(group.getId());
        member.setUserId(userId);
        member.setMemberRole(2); // 暂不区分组长和成员
        member.setJoinStatus(1); // 已同意
        member.setApproveTime(LocalDateTime.now());
        member.setApproverId(userId);
        member.setJoinTime(LocalDateTime.now());
        member.setCreatedTime(LocalDateTime.now());
        
        groupMemberMapper.insert(member);
        
        // 5. 返回小组详情
        return getGroupDetail(group.getId(), userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(Long groupId, UpdateGroupRequest request, Long userId) {
        log.info("更新小组, groupId={}, userId={}, request={}", groupId, userId, request);
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 验证权限(创建者或教师)
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, group.getCourseId());
        boolean isCreator = group.getCreatorId().equals(userId);
        boolean isTeacher = memberDTO.getMemberRole() == 1 || memberDTO.getMemberRole() == 2;
        
        if (!isCreator && !isTeacher) {
            throw new BusinessException("只有创建者或教师可以修改小组信息");
        }
        
        // 3. 更新小组信息
        CommunityGroup updateGroup = new CommunityGroup();
        updateGroup.setId(groupId);
        updateGroup.setGroupName(request.getGroupName());
        updateGroup.setGroupIntro(request.getGroupIntro());
        if (request.getMaxMembers() != null) {
            // 检查最大成员数不能小于当前成员数
            if (request.getMaxMembers() < group.getMemberCount()) {
                throw new BusinessException("最大成员数不能小于当前成员数");
            }
            updateGroup.setMaxMembers(request.getMaxMembers());
        }
        updateGroup.setUpdatedTime(LocalDateTime.now());
        
        groupMapper.updateById(updateGroup);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long groupId, Long userId) {
        log.info("解散小组, groupId={}, userId={}", groupId, userId);
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 验证权限(创建者或教师)
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, group.getCourseId());
        boolean isCreator = group.getCreatorId().equals(userId);
        boolean isTeacher = memberDTO.getMemberRole() == 1 || memberDTO.getMemberRole() == 2;
        
        if (!isCreator && !isTeacher) {
            throw new BusinessException("只有创建者或教师可以解散小组");
        }
        
        // 3. 逻辑删除小组
        CommunityGroup updateGroup = new CommunityGroup();
        updateGroup.setId(groupId);
        updateGroup.setStatus(0); // 状态改为解散
        updateGroup.setIsDeleted(1);
        updateGroup.setUpdatedTime(LocalDateTime.now());
        groupMapper.updateById(updateGroup);
        
        // 4. 逻辑删除小组内所有帖子
        LambdaUpdateWrapper<CommunityPost> postWrapper = new LambdaUpdateWrapper<>();
        postWrapper.eq(CommunityPost::getGroupId, groupId)
                   .set(CommunityPost::getIsDeleted, 1)
                   .set(CommunityPost::getUpdatedTime, LocalDateTime.now());
        postMapper.update(null, postWrapper);
        
        log.info("小组解散成功, groupId={}, 相关帖子已逻辑删除", groupId);
    }
    
    @Override
    public GroupDetailResponse getGroupDetail(Long groupId, Long userId) {
        log.info("查询小组详情, groupId={}, userId={}", groupId, userId);
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 构建响应
        GroupDetailResponse response = new GroupDetailResponse();
        response.setGroupId(group.getId());
        response.setCourseId(group.getCourseId());
        response.setGroupName(group.getGroupName());
        response.setGroupIntro(group.getGroupIntro());
        response.setCreatorId(group.getCreatorId());
        response.setMaxMembers(group.getMaxMembers());
        response.setMemberCount(group.getMemberCount());
        response.setStatus(group.getStatus());
        response.setCreatedTime(group.getCreatedTime());
        
        // 3. 查询创建者信息
        if (userServiceClient != null) {
            try {
                List<Long> userIds = List.of(group.getCreatorId());
                Map<Long, UserInfoDTO> userMap = userServiceClient.batchGetUserInfo(userIds).getData();
                if (userMap != null && userMap.containsKey(group.getCreatorId())) {
                    UserInfoDTO creator = userMap.get(group.getCreatorId());
                    response.setCreatorName(creator.getRealName());
                    response.setCreatorAvatar(creator.getAvatarUrl());
                }
            } catch (Exception e) {
                log.warn("获取创建者信息失败, creatorId={}", group.getCreatorId(), e);
                response.setCreatorName("未知用户");
            }
        } else {
            response.setCreatorName("未知用户");
        }
        
        // 4. 查询当前用户的加入状态
        CommunityGroupMember member = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<CommunityGroupMember>()
                .eq(CommunityGroupMember::getGroupId, groupId)
                .eq(CommunityGroupMember::getUserId, userId)
        );
        
        if (member != null) {
            response.setIsJoined(member.getJoinStatus() == 1);
            response.setJoinStatus(member.getJoinStatus());
        } else {
            response.setIsJoined(false);
            response.setJoinStatus(null);
        }
        
        return response;
    }
    
    @Override
    public Page<GroupListResponse> listGroups(GroupQueryRequest request) {
        log.info("查询小组列表, request={}", request);
        
        // 1. 构建查询条件
        LambdaQueryWrapper<CommunityGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getCourseId() != null, CommunityGroup::getCourseId, request.getCourseId())
               .eq(request.getStatus() != null, CommunityGroup::getStatus, request.getStatus())
               .orderByDesc(CommunityGroup::getCreatedTime);
        
        // 2. 分页查询
        Page<CommunityGroup> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<CommunityGroup> groupPage = groupMapper.selectPage(page, wrapper);
        
        // 3. 转换为响应DTO
        Page<GroupListResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(groupPage, responsePage, "records");
        
        List<GroupListResponse> responseList = groupPage.getRecords().stream()
            .map(this::convertToListResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(responseList);
        
        return responsePage;
    }
    
    /**
     * 转换为列表响应DTO
     */
    private GroupListResponse convertToListResponse(CommunityGroup group) {
        GroupListResponse response = new GroupListResponse();
        response.setGroupId(group.getId());
        response.setGroupName(group.getGroupName());
        response.setGroupIntro(group.getGroupIntro());
        response.setMemberCount(group.getMemberCount());
        response.setMaxMembers(group.getMaxMembers());
        response.setStatus(group.getStatus());
        
        // 查询创建者信息
        if (userServiceClient != null) {
            try {
                List<Long> userIds = List.of(group.getCreatorId());
                Map<Long, UserInfoDTO> userMap = userServiceClient.batchGetUserInfo(userIds).getData();
                if (userMap != null && userMap.containsKey(group.getCreatorId())) {
                    UserInfoDTO creator = userMap.get(group.getCreatorId());
                    response.setCreatorName(creator.getRealName());
                }
            } catch (Exception e) {
                log.warn("获取创建者信息失败, creatorId={}", group.getCreatorId(), e);
                response.setCreatorName("未知用户");
            }
        } else {
            response.setCreatorName("未知用户");
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyJoinGroup(Long groupId, Long userId) {
        log.info("申请加入小组, groupId={}, userId={}", groupId, userId);
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 验证用户是否为课程成员
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, group.getCourseId());
        if (memberDTO == null || memberDTO.getJoinStatus() != 1) {
            throw new BusinessException("只有该课程成员才能申请加入小组");
        }
        
        // 3. 检查用户是否已申请或已加入
        CommunityGroupMember existingMember = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<CommunityGroupMember>()
                .eq(CommunityGroupMember::getGroupId, groupId)
                .eq(CommunityGroupMember::getUserId, userId)
        );
        
        if (existingMember != null) {
            if (existingMember.getJoinStatus() == 0) {
                throw new BusinessException("您已申请加入该小组,请等待审批");
            } else if (existingMember.getJoinStatus() == 1) {
                throw new BusinessException("您已是该小组成员");
            } else if (existingMember.getJoinStatus() == 2) {
                throw new BusinessException("您的申请已被拒绝,无法再次申请");
            }
        }
        
        // 3. 检查小组人数是否已满
        if (group.getMaxMembers() != null && group.getMemberCount() >= group.getMaxMembers()) {
            throw new BusinessException("小组人数已满");
        }
        
        // 4. 创建申请记录
        CommunityGroupMember member = new CommunityGroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setMemberRole(2); // 普通成员
        member.setJoinStatus(0); // 待审批
        member.setCreatedTime(LocalDateTime.now());
        
        groupMemberMapper.insert(member);
        log.info("申请加入小组成功, groupId={}, userId={}, memberId={}", groupId, userId, member.getId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveJoinRequest(Long groupId, Long memberId, Integer approveStatus, Long userId) {
        log.info("审批加入申请, groupId={}, memberId={}, approveStatus={}, userId={}", 
                 groupId, memberId, approveStatus, userId);
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 验证用户是否为教师
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, group.getCourseId());
        if (memberDTO.getMemberRole() != 1 && memberDTO.getMemberRole() != 2) {
            throw new BusinessException("只有教师可以审批加入申请");
        }
        
        // 3. 查询申请记录
        CommunityGroupMember member = groupMemberMapper.selectById(memberId);
        if (member == null || !member.getGroupId().equals(groupId)) {
            throw new BusinessException("申请记录不存在");
        }
        
        if (member.getJoinStatus() != 0) {
            throw new BusinessException("该申请已被处理");
        }
        
        // 4. 验证审批状态
        if (approveStatus != 1 && approveStatus != 2) {
            throw new BusinessException("审批状态无效");
        }
        
        // 5. 如果同意,检查小组人数
        if (approveStatus == 1) {
            if (group.getMaxMembers() != null && group.getMemberCount() >= group.getMaxMembers()) {
                throw new BusinessException("小组人数已满,无法通过申请");
            }
        }
        
        // 6. 更新申请状态
        CommunityGroupMember updateMember = new CommunityGroupMember();
        updateMember.setId(memberId);
        updateMember.setJoinStatus(approveStatus);
        updateMember.setApproveTime(LocalDateTime.now());
        updateMember.setApproverId(userId);
        
        if (approveStatus == 1) {
            updateMember.setJoinTime(LocalDateTime.now());
        }
        
        groupMemberMapper.updateById(updateMember);
        
        // 7. 如果同意,更新小组成员数
        if (approveStatus == 1) {
            CommunityGroup updateGroup = new CommunityGroup();
            updateGroup.setId(groupId);
            updateGroup.setMemberCount(group.getMemberCount() + 1);
            updateGroup.setUpdatedTime(LocalDateTime.now());
            groupMapper.updateById(updateGroup);
        }
        
        log.info("审批完成, memberId={}, approveStatus={}", memberId, approveStatus);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quitGroup(Long groupId, Long userId) {
        log.info("退出小组, groupId={}, userId={}", groupId, userId);
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 查询成员记录
        CommunityGroupMember member = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<CommunityGroupMember>()
                .eq(CommunityGroupMember::getGroupId, groupId)
                .eq(CommunityGroupMember::getUserId, userId)
        );
        
        if (member == null) {
            throw new BusinessException("您不是该小组成员");
        }
        
        if (member.getJoinStatus() != 1) {
            throw new BusinessException("您还未加入该小组");
        }
        
        // 3. 删除成员记录
        groupMemberMapper.deleteById(member.getId());
        
        // 4. 更新小组成员数
        CommunityGroup updateGroup = new CommunityGroup();
        updateGroup.setId(groupId);
        updateGroup.setMemberCount(Math.max(0, group.getMemberCount() - 1));
        updateGroup.setUpdatedTime(LocalDateTime.now());
        groupMapper.updateById(updateGroup);
        
        log.info("退出小组成功, groupId={}, userId={}", groupId, userId);
    }
    
    @Override
    public Page<GroupMemberResponse> listGroupMembers(Long groupId, Integer pageNum, Integer pageSize) {
        log.info("查询小组成员列表, groupId={}, pageNum={}, pageSize={}", groupId, pageNum, pageSize);
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 查询已审批通过的成员
        LambdaQueryWrapper<CommunityGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityGroupMember::getGroupId, groupId)
               .eq(CommunityGroupMember::getJoinStatus, 1)
               .orderByAsc(CommunityGroupMember::getJoinTime);
        
        Page<CommunityGroupMember> page = new Page<>(pageNum, pageSize);
        Page<CommunityGroupMember> memberPage = groupMemberMapper.selectPage(page, wrapper);
        
        // 3. 转换为响应DTO
        Page<GroupMemberResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(memberPage, responsePage, "records");
        
        List<GroupMemberResponse> responseList = memberPage.getRecords().stream()
            .map(this::convertToMemberResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(responseList);
        
        return responsePage;
    }
    
    @Override
    public Page<GroupMemberResponse> listJoinRequests(Long groupId, Integer pageNum, Integer pageSize, Long userId) {
        log.info("查询待审批申请列表, groupId={}, userId={}", groupId, userId);
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 验证用户是否为教师
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, group.getCourseId());
        if (memberDTO.getMemberRole() != 1 && memberDTO.getMemberRole() != 2) {
            throw new BusinessException("只有教师可以查看待审批申请");
        }
        
        // 3. 查询待审批的申请
        LambdaQueryWrapper<CommunityGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityGroupMember::getGroupId, groupId)
               .eq(CommunityGroupMember::getJoinStatus, 0)
               .orderByDesc(CommunityGroupMember::getCreatedTime);
        
        Page<CommunityGroupMember> page = new Page<>(pageNum, pageSize);
        Page<CommunityGroupMember> memberPage = groupMemberMapper.selectPage(page, wrapper);
        
        // 4. 转换为响应DTO
        Page<GroupMemberResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(memberPage, responsePage, "records");
        
        List<GroupMemberResponse> responseList = memberPage.getRecords().stream()
            .map(this::convertToMemberResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(responseList);
        
        return responsePage;
    }
    
    /**
     * 转换为成员响应DTO
     */
    private GroupMemberResponse convertToMemberResponse(CommunityGroupMember member) {
        GroupMemberResponse response = new GroupMemberResponse();
        response.setMemberId(member.getId());
        response.setUserId(member.getUserId());
        response.setMemberRole(member.getMemberRole());
        response.setJoinStatus(member.getJoinStatus());
        response.setJoinTime(member.getJoinTime());
        
        // 查询用户信息
        if (userServiceClient != null) {
            try {
                List<Long> userIds = List.of(member.getUserId());
                Map<Long, UserInfoDTO> userMap = userServiceClient.batchGetUserInfo(userIds).getData();
                if (userMap != null && userMap.containsKey(member.getUserId())) {
                    UserInfoDTO user = userMap.get(member.getUserId());
                    response.setUserName(user.getRealName());
                    response.setUserAvatar(user.getAvatarUrl());
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败, userId={}", member.getUserId(), e);
                response.setUserName("未知用户");
            }
        } else {
            response.setUserName("未知用户");
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMemberByTeacher(Long groupId, Long targetUserId, Long operatorId) {
        log.info("教师手动添加成员, groupId={}, targetUserId={}, operatorId={}", groupId, targetUserId, operatorId);
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 验证操作人是否为教师
        CourseMemberDTO operatorMember = permissionUtil.checkCourseMember(operatorId, group.getCourseId());
        if (operatorMember.getMemberRole() != 1 && operatorMember.getMemberRole() != 2) {
            throw new BusinessException("只有教师可以手动添加成员");
        }
        
        // 3. 验证目标用户是否为课程成员
        CourseMemberDTO targetMember = permissionUtil.checkCourseMember(targetUserId, group.getCourseId());
        if (targetMember == null || targetMember.getJoinStatus() != 1) {
            throw new BusinessException("只能添加该课程的成员到小组");
        }
        
        // 4. 检查用户是否已经是小组成员
        CommunityGroupMember existingMember = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<CommunityGroupMember>()
                .eq(CommunityGroupMember::getGroupId, groupId)
                .eq(CommunityGroupMember::getUserId, targetUserId)
        );
        
        if (existingMember != null) {
            if (existingMember.getJoinStatus() == 1) {
                throw new BusinessException("该用户已是小组成员");
            } else if (existingMember.getJoinStatus() == 0) {
                // 如果有待审批的申请,直接更新为已通过
                CommunityGroupMember updateMember = new CommunityGroupMember();
                updateMember.setId(existingMember.getId());
                updateMember.setJoinStatus(1);
                updateMember.setApproveTime(LocalDateTime.now());
                updateMember.setApproverId(operatorId);
                updateMember.setJoinTime(LocalDateTime.now());
                groupMemberMapper.updateById(updateMember);
                
                // 更新小组成员数
                CommunityGroup updateGroup = new CommunityGroup();
                updateGroup.setId(groupId);
                updateGroup.setMemberCount(group.getMemberCount() + 1);
                updateGroup.setUpdatedTime(LocalDateTime.now());
                groupMapper.updateById(updateGroup);
                
                log.info("已将待审批申请直接通过, memberId={}", existingMember.getId());
                return;
            } else if (existingMember.getJoinStatus() == 2) {
                // 如果之前被拒绝,更新为已通过
                CommunityGroupMember updateMember = new CommunityGroupMember();
                updateMember.setId(existingMember.getId());
                updateMember.setJoinStatus(1);
                updateMember.setApproveTime(LocalDateTime.now());
                updateMember.setApproverId(operatorId);
                updateMember.setJoinTime(LocalDateTime.now());
                groupMemberMapper.updateById(updateMember);
                
                // 更新小组成员数
                CommunityGroup updateGroup = new CommunityGroup();
                updateGroup.setId(groupId);
                updateGroup.setMemberCount(group.getMemberCount() + 1);
                updateGroup.setUpdatedTime(LocalDateTime.now());
                groupMapper.updateById(updateGroup);
                
                log.info("已将被拒绝的申请更新为通过, memberId={}", existingMember.getId());
                return;
            }
        }
        
        // 5. 检查小组人数是否已满
        if (group.getMaxMembers() != null && group.getMemberCount() >= group.getMaxMembers()) {
            throw new BusinessException("小组人数已满");
        }
        
        // 6. 直接添加成员(已审批通过状态)
        CommunityGroupMember member = new CommunityGroupMember();
        member.setGroupId(groupId);
        member.setUserId(targetUserId);
        member.setMemberRole(2); // 普通成员
        member.setJoinStatus(1); // 已同意
        member.setApproveTime(LocalDateTime.now());
        member.setApproverId(operatorId);
        member.setJoinTime(LocalDateTime.now());
        member.setCreatedTime(LocalDateTime.now());
        
        groupMemberMapper.insert(member);
        
        // 7. 更新小组成员数
        CommunityGroup updateGroup = new CommunityGroup();
        updateGroup.setId(groupId);
        updateGroup.setMemberCount(group.getMemberCount() + 1);
        updateGroup.setUpdatedTime(LocalDateTime.now());
        groupMapper.updateById(updateGroup);
        
        log.info("教师手动添加成员成功, groupId={}, targetUserId={}, memberId={}", groupId, targetUserId, member.getId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMemberByTeacher(Long groupId, Long targetUserId, Long operatorId) {
        log.info("教师手动移除成员, groupId={}, targetUserId={}, operatorId={}", groupId, targetUserId, operatorId);
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 验证操作人是否为教师
        CourseMemberDTO operatorMember = permissionUtil.checkCourseMember(operatorId, group.getCourseId());
        if (operatorMember.getMemberRole() != 1 && operatorMember.getMemberRole() != 2) {
            throw new BusinessException("只有教师可以手动移除成员");
        }
        
        // 3. 不能移除创建者
        if (targetUserId.equals(group.getCreatorId())) {
            throw new BusinessException("不能移除小组创建者");
        }
        
        // 4. 查询成员记录
        CommunityGroupMember member = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<CommunityGroupMember>()
                .eq(CommunityGroupMember::getGroupId, groupId)
                .eq(CommunityGroupMember::getUserId, targetUserId)
        );
        
        if (member == null) {
            throw new BusinessException("该用户不是小组成员");
        }
        
        // 5. 删除成员记录
        groupMemberMapper.deleteById(member.getId());
        
        // 6. 更新小组成员数(仅当成员是已通过状态时才减少)
        if (member.getJoinStatus() == 1) {
            CommunityGroup updateGroup = new CommunityGroup();
            updateGroup.setId(groupId);
            updateGroup.setMemberCount(Math.max(0, group.getMemberCount() - 1));
            updateGroup.setUpdatedTime(LocalDateTime.now());
            groupMapper.updateById(updateGroup);
        }
        
        log.info("教师手动移除成员成功, groupId={}, targetUserId={}, memberId={}", groupId, targetUserId, member.getId());
    }
}

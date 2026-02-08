package com.edu.platform.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.Result;
import com.edu.platform.community.client.UserServiceClient;
import com.edu.platform.community.dto.request.CreateTopicRequest;
import com.edu.platform.community.dto.request.UpdateTopicRequest;
import com.edu.platform.community.dto.response.CourseMemberDTO;
import com.edu.platform.community.dto.response.TopicDetailResponse;
import com.edu.platform.community.dto.response.TopicListResponse;
import com.edu.platform.community.dto.response.UserInfoDTO;
import com.edu.platform.community.entity.CommunityGroup;
import com.edu.platform.community.entity.GroupTopic;
import com.edu.platform.community.mapper.CommunityGroupMapper;
import com.edu.platform.community.mapper.GroupTopicMapper;
import com.edu.platform.community.service.GroupTopicService;
import com.edu.platform.community.util.PermissionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小组话题Service实现类
 */
@Slf4j
@Service
public class GroupTopicServiceImpl implements GroupTopicService {
    
    private final GroupTopicMapper topicMapper;
    private final CommunityGroupMapper groupMapper;
    private final PermissionUtil permissionUtil;
    private final UserServiceClient userServiceClient;
    
    public GroupTopicServiceImpl(GroupTopicMapper topicMapper,
                                  CommunityGroupMapper groupMapper,
                                  PermissionUtil permissionUtil,
                                  UserServiceClient userServiceClient) {
        this.topicMapper = topicMapper;
        this.groupMapper = groupMapper;
        this.permissionUtil = permissionUtil;
        this.userServiceClient = userServiceClient;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTopic(Long groupId, CreateTopicRequest request, Long userId) {
        log.info("创建话题, groupId={}, userId={}, title={}", groupId, userId, request.getTitle());
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 验证用户是否为教师
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, group.getCourseId());
        if (memberDTO.getMemberRole() != 1 && memberDTO.getMemberRole() != 2) {
            throw new BusinessException("只有教师可以创建话题");
        }
        
        // 3. 创建话题
        GroupTopic topic = new GroupTopic();
        topic.setGroupId(groupId);
        topic.setCourseId(group.getCourseId());
        topic.setCreatorId(userId);
        topic.setTitle(request.getTitle());
        topic.setContent(request.getContent());
        topic.setDeadline(request.getDeadline());
        topic.setStatus(1); // 默认进行中
        topic.setCreatedTime(LocalDateTime.now());
        topic.setUpdatedTime(LocalDateTime.now());
        
        topicMapper.insert(topic);
        
        log.info("创建话题成功, topicId={}", topic.getId());
        return topic.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTopic(Long groupId, Long topicId, UpdateTopicRequest request, Long userId) {
        log.info("更新话题, groupId={}, topicId={}, userId={}", groupId, topicId, userId);
        
        // 1. 查询话题
        GroupTopic topic = topicMapper.selectById(topicId);
        if (topic == null || topic.getIsDeleted() == 1) {
            throw new BusinessException("话题不存在");
        }
        
        // 2. 验证话题属于该小组
        if (!topic.getGroupId().equals(groupId)) {
            throw new BusinessException("话题不属于该小组");
        }
        
        // 3. 验证用户是否为教师
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, topic.getCourseId());
        if (memberDTO.getMemberRole() != 1 && memberDTO.getMemberRole() != 2) {
            throw new BusinessException("只有教师可以更新话题");
        }
        
        // 4. 更新话题
        GroupTopic updateTopic = new GroupTopic();
        updateTopic.setId(topicId);
        if (request.getTitle() != null) {
            updateTopic.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            updateTopic.setContent(request.getContent());
        }
        if (request.getDeadline() != null) {
            updateTopic.setDeadline(request.getDeadline());
        }
        if (request.getStatus() != null) {
            updateTopic.setStatus(request.getStatus());
        }
        updateTopic.setUpdatedTime(LocalDateTime.now());
        
        topicMapper.updateById(updateTopic);
        
        log.info("更新话题成功, topicId={}", topicId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(Long groupId, Long topicId, Long userId) {
        log.info("删除话题, groupId={}, topicId={}, userId={}", groupId, topicId, userId);
        
        // 1. 查询话题
        GroupTopic topic = topicMapper.selectById(topicId);
        if (topic == null || topic.getIsDeleted() == 1) {
            throw new BusinessException("话题不存在");
        }
        
        // 2. 验证话题属于该小组
        if (!topic.getGroupId().equals(groupId)) {
            throw new BusinessException("话题不属于该小组");
        }
        
        // 3. 验证用户是否为教师
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, topic.getCourseId());
        if (memberDTO.getMemberRole() != 1 && memberDTO.getMemberRole() != 2) {
            throw new BusinessException("只有教师可以删除话题");
        }
        
        // 4. 逻辑删除话题
        topicMapper.deleteById(topicId);
        
        log.info("删除话题成功, topicId={}", topicId);
    }
    
    @Override
    public TopicDetailResponse getTopicDetail(Long groupId, Long topicId, Long userId) {
        log.info("获取话题详情, groupId={}, topicId={}, userId={}", groupId, topicId, userId);
        
        // 1. 查询话题
        GroupTopic topic = topicMapper.selectById(topicId);
        if (topic == null || topic.getIsDeleted() == 1) {
            throw new BusinessException("话题不存在");
        }
        
        // 2. 验证话题属于该小组
        if (!topic.getGroupId().equals(groupId)) {
            throw new BusinessException("话题不属于该小组");
        }
        
        // 3. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 4. 验证用户权限(小组成员或教师)
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, topic.getCourseId());
        boolean isTeacher = memberDTO.getMemberRole() == 1 || memberDTO.getMemberRole() == 2;
        
        // 如果不是教师,需要验证是否为小组成员
        if (!isTeacher) {
            boolean isMember = permissionUtil.isGroupMember(userId, groupId);
            if (!isMember) {
                throw new BusinessException("只有小组成员和教师可以查看话题");
            }
        }
        
        // 5. 构建响应
        TopicDetailResponse response = new TopicDetailResponse();
        response.setTopicId(topic.getId());
        response.setGroupId(topic.getGroupId());
        response.setGroupName(group.getGroupName());
        response.setCourseId(topic.getCourseId());
        response.setCreatorId(topic.getCreatorId());
        response.setTitle(topic.getTitle());
        response.setContent(topic.getContent());
        response.setDeadline(topic.getDeadline());
        response.setStatus(topic.getStatus());
        response.setCreatedTime(topic.getCreatedTime());
        response.setUpdatedTime(topic.getUpdatedTime());
        
        // 6. 获取创建者信息
        try {
            Result<UserInfoDTO> result = userServiceClient.getUserById(topic.getCreatorId());
            if (result != null && result.getData() != null) {
                response.setCreatorName(result.getData().getRealName());
            } else {
                response.setCreatorName("未知用户");
            }
        } catch (Exception e) {
            log.warn("获取创建者信息失败, userId={}", topic.getCreatorId(), e);
            response.setCreatorName("未知用户");
        }
        
        return response;
    }
    
    @Override
    public Page<TopicListResponse> listTopicsByGroup(Long groupId, Integer pageNum, Integer pageSize, Long userId) {
        log.info("查询小组话题列表, groupId={}, pageNum={}, pageSize={}, userId={}", groupId, pageNum, pageSize, userId);
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 验证用户权限(小组成员或教师)
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, group.getCourseId());
        boolean isTeacher = memberDTO.getMemberRole() == 1 || memberDTO.getMemberRole() == 2;
        
        // 如果不是教师,需要验证是否为小组成员
        if (!isTeacher) {
            boolean isMember = permissionUtil.isGroupMember(userId, groupId);
            if (!isMember) {
                throw new BusinessException("只有小组成员和教师可以查看话题列表");
            }
        }
        
        // 3. 分页查询话题
        Page<GroupTopic> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GroupTopic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupTopic::getGroupId, groupId)
                    .orderByDesc(GroupTopic::getCreatedTime);
        
        Page<GroupTopic> topicPage = topicMapper.selectPage(page, queryWrapper);
        
        // 4. 转换为响应对象
        Page<TopicListResponse> responsePage = new Page<>(topicPage.getCurrent(), topicPage.getSize(), topicPage.getTotal());
        List<TopicListResponse> responseList = convertToListResponse(topicPage.getRecords(), group);
        responsePage.setRecords(responseList);
        
        return responsePage;
    }
    
    @Override
    public Page<TopicListResponse> listTopicsByCourse(Long courseId, Integer pageNum, Integer pageSize, Long userId) {
        log.info("查询课程话题列表, courseId={}, pageNum={}, pageSize={}, userId={}", courseId, pageNum, pageSize, userId);
        
        // 1. 验证用户是否为课程成员
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, courseId);
        
        // 2. 分页查询话题
        Page<GroupTopic> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GroupTopic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupTopic::getCourseId, courseId)
                    .orderByDesc(GroupTopic::getCreatedTime);
        
        Page<GroupTopic> topicPage = topicMapper.selectPage(page, queryWrapper);
        
        // 3. 获取所有小组信息
        List<Long> groupIds = topicPage.getRecords().stream()
                .map(GroupTopic::getGroupId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, CommunityGroup> groupMap = null;
        if (!groupIds.isEmpty()) {
            List<CommunityGroup> groups = groupMapper.selectBatchIds(groupIds);
            groupMap = groups.stream().collect(Collectors.toMap(CommunityGroup::getId, g -> g));
        }
        
        // 4. 转换为响应对象
        Page<TopicListResponse> responsePage = new Page<>(topicPage.getCurrent(), topicPage.getSize(), topicPage.getTotal());
        List<TopicListResponse> responseList = convertToListResponse(topicPage.getRecords(), groupMap);
        responsePage.setRecords(responseList);
        
        return responsePage;
    }
    
    /**
     * 转换为列表响应(单个小组)
     */
    private List<TopicListResponse> convertToListResponse(List<GroupTopic> topics, CommunityGroup group) {
        if (topics == null || topics.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有创建者ID
        List<Long> creatorIds = topics.stream()
                .map(GroupTopic::getCreatorId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量获取用户信息
        Map<Long, UserInfoDTO> userMap = null;
        try {
            Result<Map<Long, UserInfoDTO>> result = userServiceClient.batchGetUserInfo(creatorIds);
            if (result != null && result.getData() != null) {
                userMap = result.getData();
            }
        } catch (Exception e) {
            log.warn("批量获取用户信息失败", e);
        }
        
        // 转换
        List<TopicListResponse> responseList = new ArrayList<>();
        for (GroupTopic topic : topics) {
            TopicListResponse response = new TopicListResponse();
            response.setTopicId(topic.getId());
            response.setGroupId(topic.getGroupId());
            response.setGroupName(group.getGroupName());
            response.setCreatorId(topic.getCreatorId());
            response.setTitle(topic.getTitle());
            response.setDeadline(topic.getDeadline());
            response.setStatus(topic.getStatus());
            response.setCreatedTime(topic.getCreatedTime());
            
            // 设置创建者姓名
            if (userMap != null && userMap.containsKey(topic.getCreatorId())) {
                response.setCreatorName(userMap.get(topic.getCreatorId()).getRealName());
            } else {
                response.setCreatorName("未知用户");
            }
            
            responseList.add(response);
        }
        
        return responseList;
    }
    
    /**
     * 转换为列表响应(多个小组)
     */
    private List<TopicListResponse> convertToListResponse(List<GroupTopic> topics, Map<Long, CommunityGroup> groupMap) {
        if (topics == null || topics.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有创建者ID
        List<Long> creatorIds = topics.stream()
                .map(GroupTopic::getCreatorId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量获取用户信息
        Map<Long, UserInfoDTO> userMap = null;
        try {
            Result<Map<Long, UserInfoDTO>> result = userServiceClient.batchGetUserInfo(creatorIds);
            if (result != null && result.getData() != null) {
                userMap = result.getData();
            }
        } catch (Exception e) {
            log.warn("批量获取用户信息失败", e);
        }
        
        // 转换
        List<TopicListResponse> responseList = new ArrayList<>();
        for (GroupTopic topic : topics) {
            TopicListResponse response = new TopicListResponse();
            response.setTopicId(topic.getId());
            response.setGroupId(topic.getGroupId());
            
            // 设置小组名称
            if (groupMap != null && groupMap.containsKey(topic.getGroupId())) {
                response.setGroupName(groupMap.get(topic.getGroupId()).getGroupName());
            } else {
                response.setGroupName("未知小组");
            }
            
            response.setCreatorId(topic.getCreatorId());
            response.setTitle(topic.getTitle());
            response.setDeadline(topic.getDeadline());
            response.setStatus(topic.getStatus());
            response.setCreatedTime(topic.getCreatedTime());
            
            // 设置创建者姓名
            if (userMap != null && userMap.containsKey(topic.getCreatorId())) {
                response.setCreatorName(userMap.get(topic.getCreatorId()).getRealName());
            } else {
                response.setCreatorName("未知用户");
            }
            
            responseList.add(response);
        }
        
        return responseList;
    }
}

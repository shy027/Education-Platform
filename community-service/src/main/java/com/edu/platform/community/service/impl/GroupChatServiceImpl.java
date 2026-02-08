package com.edu.platform.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.Result;
import com.edu.platform.community.client.UserServiceClient;
import com.edu.platform.community.dto.request.SendChatMessageRequest;
import com.edu.platform.community.dto.response.ChatMessageResponse;
import com.edu.platform.community.dto.response.CourseMemberDTO;
import com.edu.platform.community.dto.response.UserInfoDTO;
import com.edu.platform.community.entity.CommunityGroup;
import com.edu.platform.community.entity.GroupChatMessage;
import com.edu.platform.community.mapper.CommunityGroupMapper;
import com.edu.platform.community.mapper.GroupChatMessageMapper;
import com.edu.platform.community.service.GroupChatService;
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
 * 小组聊天Service实现类
 */
@Slf4j
@Service
public class GroupChatServiceImpl implements GroupChatService {
    
    private final GroupChatMessageMapper chatMessageMapper;
    private final CommunityGroupMapper groupMapper;
    private final PermissionUtil permissionUtil;
    private final UserServiceClient userServiceClient;
    
    public GroupChatServiceImpl(GroupChatMessageMapper chatMessageMapper,
                                 CommunityGroupMapper groupMapper,
                                 PermissionUtil permissionUtil,
                                 UserServiceClient userServiceClient) {
        this.chatMessageMapper = chatMessageMapper;
        this.groupMapper = groupMapper;
        this.permissionUtil = permissionUtil;
        this.userServiceClient = userServiceClient;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long groupId, SendChatMessageRequest request, Long userId) {
        log.info("发送聊天消息, groupId={}, userId={}, messageType={}", groupId, userId, request.getMessageType());
        
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
                throw new BusinessException("只有小组成员和教师可以发送消息");
            }
        }
        
        // 3. 验证消息内容
        if (request.getMessageType() == 1 && (request.getContent() == null || request.getContent().trim().isEmpty())) {
            throw new BusinessException("文本消息内容不能为空");
        }
        if ((request.getMessageType() == 2 || request.getMessageType() == 3) && 
            (request.getFileUrl() == null || request.getFileUrl().trim().isEmpty())) {
            throw new BusinessException("图片/文件消息必须包含文件URL");
        }
        
        // 4. 创建消息
        GroupChatMessage message = new GroupChatMessage();
        message.setGroupId(groupId);
        message.setTopicId(request.getTopicId());
        message.setSenderId(userId);
        message.setMessageType(request.getMessageType());
        message.setContent(request.getContent());
        message.setFileUrl(request.getFileUrl());
        message.setFileName(request.getFileName());
        message.setFileSize(request.getFileSize());
        message.setCreatedTime(LocalDateTime.now());
        
        chatMessageMapper.insert(message);
        
        log.info("发送聊天消息成功, messageId={}", message.getId());
        return message.getId();
    }
    
    @Override
    public Page<ChatMessageResponse> getMessageHistory(Long groupId, Integer pageNum, Integer pageSize, Long userId) {
        log.info("获取聊天记录, groupId={}, pageNum={}, pageSize={}, userId={}", groupId, pageNum, pageSize, userId);
        
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
                throw new BusinessException("只有小组成员和教师可以查看聊天记录");
            }
        }
        
        // 3. 分页查询消息
        Page<GroupChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GroupChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupChatMessage::getGroupId, groupId)
                    .orderByDesc(GroupChatMessage::getCreatedTime);
        
        Page<GroupChatMessage> messagePage = chatMessageMapper.selectPage(page, queryWrapper);
        
        // 4. 转换为响应对象
        Page<ChatMessageResponse> responsePage = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        List<ChatMessageResponse> responseList = convertToResponse(messagePage.getRecords());
        responsePage.setRecords(responseList);
        
        return responsePage;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long groupId, Long messageId, Long userId) {
        log.info("删除聊天消息, groupId={}, messageId={}, userId={}", groupId, messageId, userId);
        
        // 1. 查询消息
        GroupChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null || message.getIsDeleted() == 1) {
            throw new BusinessException("消息不存在");
        }
        
        // 2. 验证消息属于该小组
        if (!message.getGroupId().equals(groupId)) {
            throw new BusinessException("消息不属于该小组");
        }
        
        // 3. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 4. 验证用户权限(仅发送者或教师)
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, group.getCourseId());
        boolean isTeacher = memberDTO.getMemberRole() == 1 || memberDTO.getMemberRole() == 2;
        boolean isSender = message.getSenderId().equals(userId);
        
        if (!isTeacher && !isSender) {
            throw new BusinessException("只有消息发送者或教师可以删除消息");
        }
        
        // 5. 逻辑删除消息
        chatMessageMapper.deleteById(messageId);
        
        log.info("删除聊天消息成功, messageId={}", messageId);
    }
    
    /**
     * 转换为响应对象
     */
    private List<ChatMessageResponse> convertToResponse(List<GroupChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有发送者ID
        List<Long> senderIds = messages.stream()
                .map(GroupChatMessage::getSenderId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量获取用户信息
        Map<Long, UserInfoDTO> userMap = null;
        try {
            Result<Map<Long, UserInfoDTO>> result = userServiceClient.batchGetUserInfo(senderIds);
            if (result != null && result.getData() != null) {
                userMap = result.getData();
            }
        } catch (Exception e) {
            log.warn("批量获取用户信息失败", e);
        }
        
        // 转换
        List<ChatMessageResponse> responseList = new ArrayList<>();
        for (GroupChatMessage message : messages) {
            ChatMessageResponse response = new ChatMessageResponse();
            response.setMessageId(message.getId());
            response.setGroupId(message.getGroupId());
            response.setTopicId(message.getTopicId());
            response.setSenderId(message.getSenderId());
            response.setMessageType(message.getMessageType());
            response.setContent(message.getContent());
            response.setFileUrl(message.getFileUrl());
            response.setFileName(message.getFileName());
            response.setFileSize(message.getFileSize());
            response.setCreatedTime(message.getCreatedTime());
            
            // 设置发送者信息
            if (userMap != null && userMap.containsKey(message.getSenderId())) {
                UserInfoDTO userInfo = userMap.get(message.getSenderId());
                response.setSenderName(userInfo.getRealName());
                response.setSenderAvatar(userInfo.getAvatarUrl()); // 使用avatarUrl字段
            } else {
                response.setSenderName("未知用户");
                response.setSenderAvatar(null);
            }
            
            responseList.add(response);
        }
        
        return responseList;
    }
}

package com.edu.platform.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.Result;
import com.edu.platform.community.client.UserServiceClient;
import com.edu.platform.community.config.EasemobConfig;
import com.edu.platform.community.dto.request.SendEasemobMessageRequest;
import com.edu.platform.community.dto.response.CourseMemberDTO;
import com.edu.platform.community.dto.response.EasemobCredentialsResponse;
import com.edu.platform.community.dto.response.UserInfoDTO;
import com.edu.platform.community.entity.CommunityGroup;
import com.edu.platform.community.entity.CommunityGroupMember;
import com.edu.platform.community.entity.GroupChatMessage;
import com.edu.platform.community.mapper.CommunityGroupMapper;
import com.edu.platform.community.mapper.CommunityGroupMemberMapper;
import com.edu.platform.community.mapper.GroupChatMessageMapper;
import com.edu.platform.community.service.EasemobChatService;
import com.edu.platform.community.service.EasemobImService;
import com.edu.platform.community.util.PermissionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 环信聊天服务实现类
 */
@Slf4j
@Service
public class EasemobChatServiceImpl implements EasemobChatService {
    
    @Autowired
    private EasemobImService easemobImService;
    
    @Autowired
    private EasemobConfig easemobConfig;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private CommunityGroupMapper groupMapper;
    
    @Autowired
    private CommunityGroupMemberMapper groupMemberMapper;
    
    @Autowired
    private GroupChatMessageMapper chatMessageMapper;
    
    @Autowired
    private PermissionUtil permissionUtil;
    
    @Override
    public void registerUser(Long userId, String password) {
        log.info("注册用户到环信, userId={}", userId);
        
        try {
            // 获取用户信息
            Result<Map<Long, UserInfoDTO>> result = userServiceClient.batchGetUserInfo(List.of(userId));
            if (result == null || result.getData() == null || !result.getData().containsKey(userId)) {
                throw new BusinessException("获取用户信息失败");
            }
            
            UserInfoDTO userInfo = result.getData().get(userId);
            
            // 注册到环信
            easemobImService.registerUser(
                String.valueOf(userId),
                password,
                userInfo.getRealName()
            );
            
            log.info("用户注册到环信成功, userId={}", userId);
        } catch (IOException e) {
            log.error("用户注册到环信失败, userId={}", userId, e);
            throw new BusinessException("注册环信用户失败: " + e.getMessage());
        }
    }
    
    @Override
    public EasemobCredentialsResponse getCredentials(Long userId) {
        log.info("获取环信登录凭证, userId={}", userId);
        
        try {
            // 1. 确保用户已注册到环信(未注册则自动注册)
            ensureUserRegisteredToEasemob(userId);
            
            // 2. 获取用户信息
            Result<Map<Long, UserInfoDTO>> result = userServiceClient.batchGetUserInfo(List.of(userId));
            if (result == null || result.getData() == null || !result.getData().containsKey(userId)) {
                throw new BusinessException("获取用户信息失败");
            }
            
            UserInfoDTO userInfo = result.getData().get(userId);
            
            // 3. 构建凭证响应
            EasemobCredentialsResponse response = new EasemobCredentialsResponse();
            response.setAppKey(easemobConfig.getAppKey());
            response.setUsername(String.valueOf(userId));
            response.setUserId(userId);
            response.setRealName(userInfo.getRealName());
            response.setRestApiUrl(easemobConfig.getRestApi().getBaseUrl());
            
            return response;
        } catch (Exception e) {
            log.error("获取环信登录凭证失败, userId={}", userId, e);
            throw new BusinessException("获取环信凭证失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncGroupMembers(Long groupId, Long userId) {
        log.info("同步小组成员到环信群组, groupId={}, userId={}", groupId, userId);
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 验证权限(仅教师或小组创建者)
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, group.getCourseId());
        boolean isTeacher = memberDTO.getMemberRole() == 1 || memberDTO.getMemberRole() == 2;
        boolean isCreator = group.getCreatorId().equals(userId);
        
        if (!isTeacher && !isCreator) {
            throw new BusinessException("只有教师或小组创建者可以同步成员");
        }
        
        // 3. 检查环信群组ID
        if (group.getEasemobGroupId() == null || group.getEasemobGroupId().isEmpty()) {
            throw new BusinessException("该小组未创建环信群组");
        }
        
        // 4. 查询小组成员
        LambdaQueryWrapper<CommunityGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityGroupMember::getGroupId, groupId)
                   .eq(CommunityGroupMember::getJoinStatus, 1);  // 已审批通过
        
        List<CommunityGroupMember> members = groupMemberMapper.selectList(queryWrapper);
        
        if (members.isEmpty()) {
            log.info("小组暂无成员, groupId={}", groupId);
            return;
        }
        
        // 5. 提取用户ID列表
        List<String> usernames = members.stream()
                .map(member -> String.valueOf(member.getUserId()))
                .collect(Collectors.toList());
        
        // 6. 批量添加到环信群组
        try {
            easemobImService.addGroupMembers(group.getEasemobGroupId(), usernames);
            log.info("同步小组成员到环信群组成功, groupId={}, memberCount={}", groupId, usernames.size());
        } catch (IOException e) {
            log.error("同步小组成员到环信群组失败, groupId={}", groupId, e);
            throw new BusinessException("同步成员失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(SendEasemobMessageRequest request, Long userId) {
        log.info("通过环信发送消息, groupId={}, userId={}, messageType={}", 
                 request.getGroupId(), userId, request.getMessageType());
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(request.getGroupId());
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 验证用户权限(小组成员或教师)
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, group.getCourseId());
        boolean isTeacher = memberDTO.getMemberRole() == 1 || memberDTO.getMemberRole() == 2;
        
        if (!isTeacher) {
            boolean isMember = permissionUtil.isGroupMember(userId, request.getGroupId());
            if (!isMember) {
                throw new BusinessException("只有小组成员和教师可以发送消息");
            }
        }
        
        // 3. 验证消息内容
        if (request.getMessageType() == 1 && 
            (request.getContent() == null || request.getContent().trim().isEmpty())) {
            throw new BusinessException("文本消息内容不能为空");
        }
        if ((request.getMessageType() == 2 || request.getMessageType() == 3) && 
            (request.getFileUrl() == null || request.getFileUrl().trim().isEmpty())) {
            throw new BusinessException("图片/文件消息必须包含文件URL");
        }
        
        // 4. 确保用户已注册到环信(未注册则自动注册)
        ensureUserRegisteredToEasemob(userId);
        
        // 5. 保存消息到MySQL(历史记录)
        GroupChatMessage message = new GroupChatMessage();
        message.setGroupId(request.getGroupId());
        message.setTopicId(request.getTopicId());
        message.setSenderId(userId);
        message.setMessageType(request.getMessageType());
        message.setContent(request.getContent());
        message.setFileUrl(request.getFileUrl());
        message.setFileName(request.getFileName());
        message.setFileSize(request.getFileSize());
        message.setCreatedTime(LocalDateTime.now());
        
        chatMessageMapper.insert(message);
        
        // 6. 通过环信发送实时消息
        if (group.getEasemobGroupId() != null && !group.getEasemobGroupId().isEmpty()) {
            try {
                String messageContent = request.getContent();
                if (request.getMessageType() == 2) {
                    messageContent = "[图片]" + (request.getFileName() != null ? request.getFileName() : "");
                } else if (request.getMessageType() == 3) {
                    messageContent = "[文件]" + (request.getFileName() != null ? request.getFileName() : "");
                }
                
                easemobImService.sendGroupMessage(
                    group.getEasemobGroupId(),
                    String.valueOf(userId),
                    messageContent
                );
                
                log.info("通过环信发送消息成功, messageId={}, easemobGroupId={}", 
                         message.getId(), group.getEasemobGroupId());
            } catch (IOException e) {
                log.error("通过环信发送消息失败, messageId={}, error={}", message.getId(), e.getMessage());
                // 环信发送失败,抛出异常让用户知道
                throw new BusinessException("消息已保存但实时推送失败: " + e.getMessage());
            }
        } else {
            log.warn("小组未创建环信群组,仅保存到MySQL, groupId={}", request.getGroupId());
            throw new BusinessException("该小组未启用实时聊天功能");
        }
        
        return message.getId();
    }
    
    @Override
    public String getEasemobHistoryMessages(Long groupId, Integer limit, Long userId) {
        log.info("获取环信历史消息, groupId={}, limit={}, userId={}", groupId, limit, userId);
        
        try {
            // 1. 查询小组
            CommunityGroup group = groupMapper.selectById(groupId);
            if (group == null || group.getIsDeleted() == 1) {
                throw new BusinessException("小组不存在");
            }
            
            // 2. 验证用户权限(小组成员或教师)
            CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, group.getCourseId());
            boolean isTeacher = memberDTO.getMemberRole() == 1 || memberDTO.getMemberRole() == 2;
            
            if (!isTeacher) {
                boolean isMember = permissionUtil.isGroupMember(userId, groupId);
                if (!isMember) {
                    throw new BusinessException("只有小组成员和教师可以查看历史消息");
                }
            }
            
            // 3. 检查小组是否有环信群组ID
            if (group.getEasemobGroupId() == null || group.getEasemobGroupId().isEmpty()) {
                throw new BusinessException("该小组未启用实时聊天功能");
            }
            
            // 4. 调用环信API获取历史消息
            String response = easemobImService.getGroupMessages(group.getEasemobGroupId(), limit);
            
            log.info("获取环信历史消息成功, groupId={}, easemobGroupId={}", groupId, group.getEasemobGroupId());
            
            return response;
        } catch (Exception e) {
            log.error("获取环信历史消息失败, groupId={}", groupId, e);
            throw new BusinessException("获取历史消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 确保用户已注册到环信
     * 如果未注册,自动注册
     */
    private void ensureUserRegisteredToEasemob(Long userId) {
        try {
            // 检查是否已注册
            boolean isRegistered = checkUserRegisteredToEasemob(userId);
            
            if (!isRegistered) {
                log.info("用户未注册到环信,开始自动注册, userId={}", userId);
                
                // 获取用户信息
                Result<Map<Long, UserInfoDTO>> result = userServiceClient.batchGetUserInfo(List.of(userId));
                if (result == null || result.getData() == null || !result.getData().containsKey(userId)) {
                    throw new BusinessException("获取用户信息失败,无法注册到环信");
                }
                
                UserInfoDTO userInfo = result.getData().get(userId);
                
                // 生成默认密码
                String defaultPassword = "easemob" + userId;
                
                // 注册到环信
                easemobImService.registerUser(
                    String.valueOf(userId),
                    defaultPassword,
                    userInfo.getRealName()
                );
                
                log.info("用户自动注册到环信成功, userId={}", userId);
            }
        } catch (Exception e) {
            log.error("确保用户注册到环信失败, userId={}", userId, e);
            throw new BusinessException("注册到聊天系统失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查用户是否已注册到环信
     */
    private boolean checkUserRegisteredToEasemob(Long userId) {
        try {
            // 调用环信API检查用户是否存在
            easemobImService.getUserInfo(String.valueOf(userId));
            return true;
        } catch (Exception e) {
            // 用户不存在或其他错误
            log.debug("用户未注册到环信, userId={}", userId);
            return false;
        }
    }
}

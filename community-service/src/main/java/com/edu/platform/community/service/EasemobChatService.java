package com.edu.platform.community.service;

import com.edu.platform.community.dto.request.SendEasemobMessageRequest;
import com.edu.platform.community.dto.response.EasemobCredentialsResponse;

/**
 * 环信聊天服务接口
 */
public interface EasemobChatService {
    
    /**
     * 注册用户到环信
     * @param userId 用户ID
     * @param password 环信登录密码
     */
    void registerUser(Long userId, String password);
    
    /**
     * 获取环信登录凭证
     * @param userId 用户ID
     * @return 环信凭证
     */
    EasemobCredentialsResponse getCredentials(Long userId);
    
    /**
     * 同步小组成员到环信群组
     * @param groupId 小组ID
     * @param userId 操作用户ID
     */
    void syncGroupMembers(Long groupId, Long userId);
    
    /**
     * 通过环信发送消息
     * @param request 消息请求
     * @param userId 发送者ID
     * @return 消息ID
     */
    Long sendMessage(SendEasemobMessageRequest request, Long userId);
    
    /**
     * 获取环信历史消息
     * @param groupId 小组ID
     * @param limit 消息数量(最大50)
     * @param userId 请求用户ID
     * @return 历史消息JSON字符串
     */
    String getEasemobHistoryMessages(Long groupId, Integer limit, Long userId);
}

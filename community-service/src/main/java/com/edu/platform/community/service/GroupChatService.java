package com.edu.platform.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.community.dto.request.SendChatMessageRequest;
import com.edu.platform.community.dto.response.ChatMessageResponse;

/**
 * 小组聊天Service接口
 */
public interface GroupChatService {
    
    /**
     * 发送消息
     *
     * @param groupId 小组ID
     * @param request 消息请求
     * @param userId 用户ID
     * @return 消息ID
     */
    Long sendMessage(Long groupId, SendChatMessageRequest request, Long userId);
    
    /**
     * 获取聊天记录
     *
     * @param groupId 小组ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param userId 用户ID
     * @return 聊天记录
     */
    Page<ChatMessageResponse> getMessageHistory(Long groupId, Integer pageNum, Integer pageSize, Long userId);
    
    /**
     * 删除消息(仅发送者或教师)
     *
     * @param groupId 小组ID
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void deleteMessage(Long groupId, Long messageId, Long userId);
}

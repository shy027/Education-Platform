package com.edu.platform.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.Result;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.community.dto.request.SendChatMessageRequest;
import com.edu.platform.community.dto.response.ChatMessageResponse;
import com.edu.platform.community.service.GroupChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 小组聊天Controller
 */
@Tag(name = "小组聊天管理")
@RestController
@RequestMapping("/api/v1/community/groups")
public class GroupChatController {
    
    private final GroupChatService chatService;
    
    public GroupChatController(GroupChatService chatService) {
        this.chatService = chatService;
    }
    
    @Operation(summary = "发送聊天消息")
    @PostMapping("/{groupId}/chat")
    public Result<Long> sendMessage(
            @PathVariable Long groupId,
            @Valid @RequestBody SendChatMessageRequest request) {
        Long userId = UserContext.getUserId();
        Long messageId = chatService.sendMessage(groupId, request, userId);
        return Result.success("发送成功", messageId);
    }
    
    @Operation(summary = "获取聊天记录")
    @GetMapping("/{groupId}/chat/history")
    public Result<Page<ChatMessageResponse>> getMessageHistory(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.getUserId();
        Page<ChatMessageResponse> page = chatService.getMessageHistory(groupId, pageNum, pageSize, userId);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "删除聊天消息")
    @DeleteMapping("/{groupId}/chat/{messageId}")
    public Result<?> deleteMessage(
            @PathVariable Long groupId,
            @PathVariable Long messageId) {
        Long userId = UserContext.getUserId();
        chatService.deleteMessage(groupId, messageId, userId);
        return Result.success("删除成功");
    }
}

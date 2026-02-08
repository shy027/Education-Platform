package com.edu.platform.community.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.community.config.EasemobConfig;
import com.edu.platform.community.dto.request.RegisterEasemobUserRequest;
import com.edu.platform.community.dto.request.SendEasemobMessageRequest;
import com.edu.platform.community.dto.response.EasemobCredentialsResponse;
import com.edu.platform.community.service.EasemobChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 环信IM控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/community/easemob")
@Tag(name = "环信IM", description = "环信实时聊天相关接口")
public class EasemobController {
    
    @Autowired
    private EasemobChatService easemobChatService;
    
    @Autowired
    private EasemobConfig easemobConfig;
    
    /**
     * 注册当前用户到环信
     */
    @Operation(summary = "注册用户到环信")
    @PostMapping("/users/register")
    public Result<?> registerUser(@Valid @RequestBody RegisterEasemobUserRequest request) {
        Long userId = UserContext.getUserId();
        easemobChatService.registerUser(userId, request.getPassword());
        return Result.success("注册成功");
    }
    
    /**
     * 获取环信登录凭证
     */
    @Operation(summary = "获取环信登录凭证")
    @GetMapping("/users/credentials")
    public Result<EasemobCredentialsResponse> getCredentials() {
        Long userId = UserContext.getUserId();
        EasemobCredentialsResponse response = easemobChatService.getCredentials(userId);
        return Result.success(response);
    }
    
    /**
     * 同步小组成员到环信群组
     */
    @Operation(summary = "同步小组成员到环信群组")
    @PostMapping("/groups/{groupId}/sync-members")
    public Result<?> syncGroupMembers(@PathVariable Long groupId) {
        Long userId = UserContext.getUserId();
        easemobChatService.syncGroupMembers(groupId, userId);
        return Result.success("同步成功");
    }
    
    /**
     * 通过环信发送消息
     */
    @Operation(summary = "通过环信发送消息")
    @PostMapping("/messages/send")
    public Result<Long> sendMessage(@Valid @RequestBody SendEasemobMessageRequest request) {
        Long userId = UserContext.getUserId();
        Long messageId = easemobChatService.sendMessage(request, userId);
        return Result.success("发送成功", messageId);
    }
    
    /**
     * 获取环信历史消息
     */
    @Operation(summary = "获取环信历史消息")
    @GetMapping("/groups/{groupId}/messages/history")
    public Result<String> getHistoryMessages(
        @PathVariable Long groupId,
        @RequestParam(defaultValue = "20") Integer limit
    ) {
        Long userId = UserContext.getUserId();
        String messages = easemobChatService.getEasemobHistoryMessages(groupId, limit, userId);
        return Result.success(messages);
    }
}

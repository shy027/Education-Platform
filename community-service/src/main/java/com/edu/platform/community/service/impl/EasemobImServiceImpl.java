package com.edu.platform.community.service.impl;

import com.edu.platform.community.service.EasemobImService;
import com.edu.platform.community.util.EasemobClient;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 环信IM服务实现类
 */
@Slf4j
@Service
public class EasemobImServiceImpl implements EasemobImService {
    
    private final EasemobClient easemobClient;
    
    public EasemobImServiceImpl(EasemobClient easemobClient) {
        this.easemobClient = easemobClient;
    }
    
    @Override
    public void registerUser(String username, String password, String nickname) throws IOException {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);
        body.put("nickname", nickname);
        
        easemobClient.sendRequest("POST", "/users", body.toString());
        log.info("注册环信用户成功: {}", username);
    }
    
    @Override
    public String createGroup(String groupName, String description, String owner, List<String> members) throws IOException {
        JSONObject body = new JSONObject();
        body.put("groupname", groupName);
        body.put("desc", description);
        body.put("public", true);  // 公开群
        body.put("maxusers", 200);  // 最大成员数
        body.put("approval", false);  // 无需审批
        body.put("owner", owner);
        
        if (members != null && !members.isEmpty()) {
            body.put("members", new JSONArray(members));
        }
        
        String response = easemobClient.sendRequest("POST", "/chatgroups", body.toString());
        JSONObject json = new JSONObject(response);
        
        String groupId = json.getJSONObject("data").getString("groupid");
        log.info("创建环信群组成功: {}, groupId: {}", groupName, groupId);
        
        return groupId;
    }
    
    @Override
    public void addGroupMembers(String groupId, List<String> usernames) throws IOException {
        if (usernames == null || usernames.isEmpty()) {
            return;
        }
        
        JSONObject body = new JSONObject();
        body.put("usernames", new JSONArray(usernames));
        
        String path = String.format("/chatgroups/%s/users", groupId);
        
        try {
            easemobClient.sendRequest("POST", path, body.toString());
            log.info("添加群组成员成功: groupId={}, count={}", groupId, usernames.size());
        } catch (IOException e) {
            // 检查是否是"用户已在群组中"的错误
            if (e.getMessage() != null && e.getMessage().contains("already in group")) {
                log.warn("部分用户已在环信群组中, groupId={}, 忽略此错误", groupId);
                // 不抛出异常,视为成功
            } else {
                // 其他错误继续抛出
                throw e;
            }
        }
    }
    
    @Override
    public void removeGroupMember(String groupId, String username) throws IOException {
        String path = String.format("/chatgroups/%s/users/%s", groupId, username);
        easemobClient.sendRequest("DELETE", path, null);
        
        log.info("移除群组成员成功: groupId={}, username={}", groupId, username);
    }
    
    @Override
    public void sendGroupMessage(String groupId, String from, String message) throws IOException {
        JSONObject body = new JSONObject();
        body.put("target_type", "chatgroups");
        body.put("target", new JSONArray().put(groupId));
        body.put("from", from);
        
        JSONObject msg = new JSONObject();
        msg.put("type", "txt");
        msg.put("msg", message);
        body.put("msg", msg);
        
        easemobClient.sendRequest("POST", "/messages", body.toString());
        log.info("发送群组消息成功: groupId={}, from={}", groupId, from);
    }
    
    @Override
    public void deleteGroup(String groupId) throws IOException {
        String path = String.format("/chatgroups/%s", groupId);
        easemobClient.sendRequest("DELETE", path, null);
        
        log.info("删除环信群组成功: groupId={}", groupId);
    }
    
    @Override
    public String getUserInfo(String username) throws IOException {
        log.info("获取环信用户信息, username={}", username);
        String path = String.format("/users/%s", username);
        return easemobClient.sendRequest("GET", path, null);
    }
    
    @Override
    public String getGroupMessages(String groupId, Integer limit) throws IOException {
        log.info("获取环信群组历史消息, groupId={}, limit={}", groupId, limit);
        
        // 限制每次最多50条
        if (limit == null || limit <= 0) {
            limit = 20;
        } else if (limit > 50) {
            limit = 50;
        }
        
        String path = String.format("/chatgroups/%s/chatmessages?limit=%d", groupId, limit);
        return easemobClient.sendRequest("GET", path, null);
    }
}

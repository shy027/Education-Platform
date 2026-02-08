package com.edu.platform.community.service;

import java.io.IOException;
import java.util.List;

/**
 * 环信IM服务接口
 */
public interface EasemobImService {
    
    /**
     * 注册环信用户
     * @param username 用户名(建议使用userId)
     * @param password 密码
     * @param nickname 昵称
     */
    void registerUser(String username, String password, String nickname) throws IOException;
    
    /**
     * 创建群组
     * @param groupName 群组名称
     * @param description 群组描述
     * @param owner 群主用户名
     * @param members 成员列表
     * @return 群组ID
     */
    String createGroup(String groupName, String description, String owner, List<String> members) throws IOException;
    
    /**
     * 添加群组成员
     * @param groupId 群组ID
     * @param usernames 用户名列表
     */
    void addGroupMembers(String groupId, List<String> usernames) throws IOException;
    
    /**
     * 移除群组成员
     * @param groupId 群组ID
     * @param username 用户名
     */
    void removeGroupMember(String groupId, String username) throws IOException;
    
    /**
     * 发送群组消息(服务端发送)
     * @param groupId 群组ID
     * @param from 发送者用户名
     * @param message 消息内容
     */
    void sendGroupMessage(String groupId, String from, String message) throws IOException;
    
    /**
     * 删除群组
     * @param groupId 群组ID
     */
    void deleteGroup(String groupId) throws IOException;
    
    /**
     * 获取环信用户信息
     * @param username 用户名
     * @return 用户信息JSON
     * @throws IOException 如果用户不存在或API调用失败
     */
    String getUserInfo(String username) throws IOException;
    
    /**
     * 获取群组历史消息
     * @param groupId 群组ID
     * @param limit 消息数量(最大50)
     * @return 历史消息JSON
     * @throws IOException 如果查询失败
     */
    String getGroupMessages(String groupId, Integer limit) throws IOException;
}

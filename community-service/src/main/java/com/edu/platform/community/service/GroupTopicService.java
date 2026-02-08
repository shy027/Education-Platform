package com.edu.platform.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.community.dto.request.CreateTopicRequest;
import com.edu.platform.community.dto.request.UpdateTopicRequest;
import com.edu.platform.community.dto.response.TopicDetailResponse;
import com.edu.platform.community.dto.response.TopicListResponse;

/**
 * 小组话题Service接口
 */
public interface GroupTopicService {
    
    /**
     * 创建话题(仅教师)
     *
     * @param groupId 小组ID
     * @param request 创建请求
     * @param userId 用户ID
     * @return 话题ID
     */
    Long createTopic(Long groupId, CreateTopicRequest request, Long userId);
    
    /**
     * 更新话题(仅教师)
     *
     * @param groupId 小组ID
     * @param topicId 话题ID
     * @param request 更新请求
     * @param userId 用户ID
     */
    void updateTopic(Long groupId, Long topicId, UpdateTopicRequest request, Long userId);
    
    /**
     * 删除话题(仅教师)
     *
     * @param groupId 小组ID
     * @param topicId 话题ID
     * @param userId 用户ID
     */
    void deleteTopic(Long groupId, Long topicId, Long userId);
    
    /**
     * 获取话题详情
     *
     * @param groupId 小组ID
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 话题详情
     */
    TopicDetailResponse getTopicDetail(Long groupId, Long topicId, Long userId);
    
    /**
     * 查询小组的话题列表
     *
     * @param groupId 小组ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param userId 用户ID
     * @return 话题列表
     */
    Page<TopicListResponse> listTopicsByGroup(Long groupId, Integer pageNum, Integer pageSize, Long userId);
    
    /**
     * 查询课程的所有话题
     *
     * @param courseId 课程ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param userId 用户ID
     * @return 话题列表
     */
    Page<TopicListResponse> listTopicsByCourse(Long courseId, Integer pageNum, Integer pageSize, Long userId);
}

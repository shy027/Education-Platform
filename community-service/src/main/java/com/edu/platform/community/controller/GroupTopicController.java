package com.edu.platform.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.Result;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.community.dto.request.CreateTopicRequest;
import com.edu.platform.community.dto.request.UpdateTopicRequest;
import com.edu.platform.community.dto.response.TopicDetailResponse;
import com.edu.platform.community.dto.response.TopicListResponse;
import com.edu.platform.community.service.GroupTopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 小组话题Controller
 */
@Tag(name = "小组话题管理")
@RestController
@RequestMapping("/api/v1/community/groups")
public class GroupTopicController {
    
    private final GroupTopicService topicService;
    
    public GroupTopicController(GroupTopicService topicService) {
        this.topicService = topicService;
    }
    
    @Operation(summary = "创建话题(仅教师)")
    @PostMapping("/{groupId}/topics")
    public Result<Long> createTopic(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateTopicRequest request) {
        Long userId = UserContext.getUserId();
        Long topicId = topicService.createTopic(groupId, request, userId);
        return Result.success("创建成功", topicId);
    }
    
    @Operation(summary = "更新话题(仅教师)")
    @PutMapping("/{groupId}/topics/{topicId}")
    public Result<?> updateTopic(
            @PathVariable Long groupId,
            @PathVariable Long topicId,
            @Valid @RequestBody UpdateTopicRequest request) {
        Long userId = UserContext.getUserId();
        topicService.updateTopic(groupId, topicId, request, userId);
        return Result.success("更新成功");
    }
    
    @Operation(summary = "删除话题(仅教师)")
    @DeleteMapping("/{groupId}/topics/{topicId}")
    public Result<?> deleteTopic(
            @PathVariable Long groupId,
            @PathVariable Long topicId) {
        Long userId = UserContext.getUserId();
        topicService.deleteTopic(groupId, topicId, userId);
        return Result.success("删除成功");
    }
    
    @Operation(summary = "获取话题详情")
    @GetMapping("/{groupId}/topics/{topicId}")
    public Result<TopicDetailResponse> getTopicDetail(
            @PathVariable Long groupId,
            @PathVariable Long topicId) {
        Long userId = UserContext.getUserId();
        TopicDetailResponse response = topicService.getTopicDetail(groupId, topicId, userId);
        return Result.success("查询成功", response);
    }
    
    @Operation(summary = "查询小组话题列表")
    @GetMapping("/{groupId}/topics")
    public Result<Page<TopicListResponse>> listTopicsByGroup(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        Page<TopicListResponse> page = topicService.listTopicsByGroup(groupId, pageNum, pageSize, userId);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "查询课程所有话题")
    @GetMapping("/courses/{courseId}/topics")
    public Result<Page<TopicListResponse>> listTopicsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        Page<TopicListResponse> page = topicService.listTopicsByCourse(courseId, pageNum, pageSize, userId);
        return Result.success("查询成功", page);
    }
}

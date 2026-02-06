package com.edu.platform.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.common.result.Result;
import com.edu.platform.community.dto.request.ApproveJoinRequest;
import com.edu.platform.community.dto.request.CreateGroupRequest;
import com.edu.platform.community.dto.request.GroupQueryRequest;
import com.edu.platform.community.dto.request.UpdateGroupRequest;
import com.edu.platform.community.dto.response.GroupDetailResponse;
import com.edu.platform.community.dto.response.GroupListResponse;
import com.edu.platform.community.dto.response.GroupMemberResponse;
import com.edu.platform.community.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 小组管理Controller
 *
 * @author Education Platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/community/groups")
@Tag(name = "小组管理", description = "小组相关接口")
public class GroupController {
    
    @Autowired
    private GroupService groupService;
    
    @Operation(summary = "创建小组(仅教师)")
    @PostMapping
    public Result<GroupDetailResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        Long userId = UserContext.getUserId();
        GroupDetailResponse response = groupService.createGroup(request, userId);
        return Result.success("小组创建成功", response);
    }
    
    @Operation(summary = "查询小组列表")
    @GetMapping
    public Result<Page<GroupListResponse>> listGroups(
            @RequestParam Long courseId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        GroupQueryRequest request = new GroupQueryRequest();
        request.setCourseId(courseId);
        request.setStatus(status);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        Page<GroupListResponse> page = groupService.listGroups(request);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "获取小组详情")
    @GetMapping("/{groupId}")
    public Result<GroupDetailResponse> getGroupDetail(@PathVariable Long groupId) {
        Long userId = UserContext.getUserId();
        GroupDetailResponse response = groupService.getGroupDetail(groupId, userId);
        return Result.success("查询成功", response);
    }
    
    @Operation(summary = "更新小组信息(创建者或教师)")
    @PutMapping("/{groupId}")
    public Result<?> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request) {
        Long userId = UserContext.getUserId();
        groupService.updateGroup(groupId, request, userId);
        return Result.success("更新成功");
    }
    
    @Operation(summary = "解散小组(创建者或教师)")
    @DeleteMapping("/{groupId}")
    public Result<?> deleteGroup(@PathVariable Long groupId) {
        Long userId = UserContext.getUserId();
        groupService.deleteGroup(groupId, userId);
        return Result.success("小组已解散");
    }
    
    @Operation(summary = "申请加入小组")
    @PostMapping("/{groupId}/apply")
    public Result<?> applyJoinGroup(@PathVariable Long groupId) {
        Long userId = UserContext.getUserId();
        groupService.applyJoinGroup(groupId, userId);
        return Result.success("申请已提交,请等待教师审批");
    }
    
    @Operation(summary = "审批加入申请(仅教师)")
    @PutMapping("/{groupId}/members/{memberId}/approve")
    public Result<?> approveJoinRequest(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @Valid @RequestBody ApproveJoinRequest request) {
        Long userId = UserContext.getUserId();
        groupService.approveJoinRequest(groupId, memberId, request.getApproveStatus(), userId);
        return Result.success("审批成功");
    }
    
    @Operation(summary = "退出小组")
    @DeleteMapping("/{groupId}/quit")
    public Result<?> quitGroup(@PathVariable Long groupId) {
        Long userId = UserContext.getUserId();
        groupService.quitGroup(groupId, userId);
        return Result.success("已退出小组");
    }
    
    @Operation(summary = "查询小组成员列表")
    @GetMapping("/{groupId}/members")
    public Result<Page<GroupMemberResponse>> listGroupMembers(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<GroupMemberResponse> page = groupService.listGroupMembers(groupId, pageNum, pageSize);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "查询待审批申请列表(仅教师)")
    @GetMapping("/{groupId}/requests")
    public Result<Page<GroupMemberResponse>> listJoinRequests(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        Page<GroupMemberResponse> page = groupService.listJoinRequests(groupId, pageNum, pageSize, userId);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "教师手动添加成员(仅教师)")
    @PostMapping("/{groupId}/members/{targetUserId}")
    public Result<?> addMemberByTeacher(
            @PathVariable Long groupId,
            @PathVariable Long targetUserId) {
        Long userId = UserContext.getUserId();
        groupService.addMemberByTeacher(groupId, targetUserId, userId);
        return Result.success("成员添加成功");
    }
    
    @Operation(summary = "教师手动移除成员(仅教师)")
    @DeleteMapping("/{groupId}/members/{targetUserId}")
    public Result<?> removeMemberByTeacher(
            @PathVariable Long groupId,
            @PathVariable Long targetUserId) {
        Long userId = UserContext.getUserId();
        groupService.removeMemberByTeacher(groupId, targetUserId, userId);
        return Result.success("成员移除成功");
    }
}

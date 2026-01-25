package com.edu.platform.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.request.AddMemberRequest;
import com.edu.platform.course.dto.request.ApproveMemberRequest;
import com.edu.platform.course.dto.request.MemberQueryRequest;
import com.edu.platform.course.dto.request.UpdateMemberRoleRequest;
import com.edu.platform.course.dto.response.MemberResponse;
import com.edu.platform.course.dto.response.MyCoursesResponse;
import com.edu.platform.course.service.CourseMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 课程成员管理接口
 */
@Tag(name = "课程成员管理")
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseMemberController {

    private final CourseMemberService memberService;

    @Operation(summary = "申请加入课程")
    @PostMapping("/{courseId}/join")
    public Result<Void> joinCourse(@PathVariable Long courseId) {
        memberService.joinCourse(courseId);
        return Result.success("申请成功", null);
    }
    
    @Operation(summary = "退出课程")
    @DeleteMapping("/{courseId}/quit")
    public Result<Void> quitCourse(@PathVariable Long courseId) {
        memberService.quitCourse(courseId);
        return Result.success("退出成功", null);
    }
    
    @Operation(summary = "审批成员")
    @PutMapping("/{courseId}/members/{userId}/approve")
    public Result<Void> approveMember(@PathVariable Long courseId, 
                                     @PathVariable Long userId,
                                     @RequestBody @Validated ApproveMemberRequest request) {
        memberService.approveMember(courseId, userId, request);
        return Result.success("审批完成", null);
    }
    
    @Operation(summary = "获取成员列表")
    @GetMapping("/{courseId}/members")
    public Result<Page<MemberResponse>> pageMembers(
            @PathVariable Long courseId,
            @RequestParam(required = false) Integer memberRole,
            @RequestParam(required = false) Integer joinStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        MemberQueryRequest request = new MemberQueryRequest();
        request.setMemberRole(memberRole);
        request.setJoinStatus(joinStatus);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        return Result.success(memberService.pageMembers(courseId, request));
    }
    
    @Operation(summary = "获取我的课程")
    @GetMapping("/my-courses")
    public Result<MyCoursesResponse> getMyCourses() {
        return Result.success(memberService.getMyCourses());
    }
    
    @Operation(summary = "添加成员")
    @PostMapping("/{courseId}/members")
    public Result<Void> addMember(@PathVariable Long courseId,
                                   @RequestBody @Validated AddMemberRequest request) {
        memberService.addMember(courseId, request);
        return Result.success("添加成功", null);
    }
    
    @Operation(summary = "移除成员")
    @DeleteMapping("/{courseId}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long courseId,
                                      @PathVariable Long userId) {
        memberService.removeMember(courseId, userId);
        return Result.success("移除成功", null);
    }
    
    @Operation(summary = "修改成员角色")
    @PutMapping("/{courseId}/members/{userId}/role")
    public Result<Void> updateMemberRole(@PathVariable Long courseId,
                                          @PathVariable Long userId,
                                          @RequestBody @Validated UpdateMemberRoleRequest request) {
        memberService.updateMemberRole(courseId, userId, request);
        return Result.success("修改成功", null);
    }
}

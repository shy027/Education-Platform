package com.edu.platform.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.platform.course.dto.request.AddMemberRequest;
import com.edu.platform.course.dto.request.ApproveMemberRequest;
import com.edu.platform.course.dto.request.MemberQueryRequest;
import com.edu.platform.course.dto.request.UpdateMemberRoleRequest;
import com.edu.platform.course.dto.response.CourseListResponse;
import com.edu.platform.course.dto.response.MemberResponse;
import com.edu.platform.course.dto.response.MyCoursesResponse;
import com.edu.platform.course.entity.CourseMember;

import java.util.List;

/**
 * 课程成员服务接口
 */
public interface CourseMemberService extends IService<CourseMember> {

    /**
     * 申请加入课程
     *
     * @param courseId 课程ID
     */
    void joinCourse(Long courseId);

    /**
     * 退出课程
     *
     * @param courseId 课程ID
     */
    void quitCourse(Long courseId);

    /**
     * 审批成员
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @param request  请求
     */
    void approveMember(Long courseId, Long userId, ApproveMemberRequest request);

    /**
     * 分页查询成员
     *
     * @param courseId 课程ID
     * @param request  查询请求
     * @return 分页结果
     */
    Page<MemberResponse> pageMembers(Long courseId, MemberQueryRequest request);
    
    /**
     * 获取我的课程列表（分类返回）
     * @return 分类课程列表
     */
    MyCoursesResponse getMyCourses();
    
    /**
     * 添加成员（教师主动添加）
     *
     * @param courseId 课程ID
     * @param request  请求
     */
    void addMember(Long courseId, AddMemberRequest request);
    
    /**
     * 移除成员
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     */
    void removeMember(Long courseId, Long userId);
    
    /**
     * 修改成员角色
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @param request  请求
     */
    void updateMemberRole(Long courseId, Long userId, UpdateMemberRoleRequest request);
    
    /**
     * 检查用户是否是课程成员
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @return 是否是成员
     */
    boolean isCourseMember(Long courseId, Long userId);
    
    /**
     * 获取成员信息
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @return 成员信息
     */
    MemberResponse getMemberInfo(Long courseId, Long userId);
}

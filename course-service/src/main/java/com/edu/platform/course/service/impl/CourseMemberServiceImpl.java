package com.edu.platform.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.course.context.UserContext;
import com.edu.platform.course.dto.request.AddMemberRequest;
import com.edu.platform.course.dto.request.ApproveMemberRequest;
import com.edu.platform.course.dto.request.MemberQueryRequest;
import com.edu.platform.course.dto.request.UpdateMemberRoleRequest;
import com.edu.platform.course.dto.response.CourseListResponse;
import com.edu.platform.course.dto.response.MemberResponse;
import com.edu.platform.course.dto.response.MyCoursesResponse;
import com.edu.platform.course.entity.Course;
import com.edu.platform.course.entity.CourseMember;
import com.edu.platform.course.mapper.CourseMemberMapper;
import com.edu.platform.course.service.CourseMemberService;
import com.edu.platform.course.service.CourseService;
import com.edu.platform.course.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程成员服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseMemberServiceImpl extends ServiceImpl<CourseMemberMapper, CourseMember> implements CourseMemberService {

    private final CourseService courseService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinCourse(Long courseId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "课程不存在");
        }
        
        if (course.getStatus() != 1) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "课程未开放");
        }
        
        // 检查是否已加入
        CourseMember exist = this.getOne(new LambdaQueryWrapper<CourseMember>()
                .eq(CourseMember::getCourseId, courseId)
                .eq(CourseMember::getUserId, userId));
                
        if (exist != null) {
            if (exist.getJoinStatus() == 1) {
                throw new BusinessException(ResultCode.FAIL.getCode(), "您已加入该课程");
            } else if (exist.getJoinStatus() == 0) {
                throw new BusinessException(ResultCode.FAIL.getCode(), "申请审核中，请勿重复申请");
            }
        }
        
        CourseMember member = (exist != null) ? exist : new CourseMember();
        member.setCourseId(courseId);
        member.setUserId(userId);
        member.setMemberRole(3); // 默认学生
        
        if (course.getJoinType() == 1) {
            // 公开课直接加入
            member.setJoinStatus(1);
            member.setJoinTime(LocalDateTime.now());
            member.setApproveTime(LocalDateTime.now());
        } else {
            // 需审批
            member.setJoinStatus(0);
            member.setJoinTime(LocalDateTime.now());
        }
        
        this.saveOrUpdate(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quitCourse(Long courseId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        CourseMember member = this.getOne(new LambdaQueryWrapper<CourseMember>()
                .eq(CourseMember::getCourseId, courseId)
                .eq(CourseMember::getUserId, userId)
                .eq(CourseMember::getJoinStatus, 1));
                
        if (member == null) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "您未加入该课程");
        }
        
        // 教师不能退出自己的课程
        Course course = courseService.getById(courseId);
        if (course != null && course.getTeacherId().equals(userId)) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "课程教师不能退出课程");
        }
        
        member.setJoinStatus(3); // 已退出
        this.updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveMember(Long courseId, Long userId, ApproveMemberRequest request) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        // 权限校验：只有课程教师、助教或管理员可以审批
        if (!hasApprovePermission(courseId, currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权审批成员");
        }
        
        // 通过 courseId 和 userId 查询成员记录
        CourseMember member = this.getOne(new LambdaQueryWrapper<CourseMember>()
                .eq(CourseMember::getCourseId, courseId)
                .eq(CourseMember::getUserId, userId));
        
        if (member == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "申请记录不存在");
        }
        
        if (member.getJoinStatus() != 0) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "该记录无需审批");
        }
        
        member.setJoinStatus(request.getApproved() ? 1 : 2); // 1通过, 2拒绝
        member.setApproveTime(LocalDateTime.now());
        member.setApproverId(currentUserId);
        
        this.updateById(member);
    }

    @Override
    public Page<MemberResponse> pageMembers(Long courseId, MemberQueryRequest request) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        Page<CourseMember> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<CourseMember> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(CourseMember::getCourseId, courseId)
                .eq(ObjectUtil.isNotNull(request.getMemberRole()), CourseMember::getMemberRole, request.getMemberRole());
        
        // 权限过滤：普通学生只能看到"已加入"的成员
        if (!hasApprovePermission(courseId, currentUserId)) {
            wrapper.eq(CourseMember::getJoinStatus, 1);
        } else {
            // 教师/助教可以看到所有状态
            wrapper.eq(ObjectUtil.isNotNull(request.getJoinStatus()), CourseMember::getJoinStatus, request.getJoinStatus());
        }
        
        wrapper.orderByDesc(CourseMember::getJoinTime);
                
        Page<CourseMember> memberPage = this.page(page, wrapper);
        
        List<MemberResponse> list = memberPage.getRecords().stream().map(m -> {
            MemberResponse resp = new MemberResponse();
            BeanUtil.copyProperties(m, resp);
            // TODO: 远程获取用户信息 (username, realName, avatar)
            resp.setUsername("User" + m.getUserId());
            resp.setRealName("Name" + m.getUserId());
            return resp;
        }).collect(Collectors.toList());
        
        Page<MemberResponse> result = new Page<>();
        BeanUtil.copyProperties(memberPage, result, "records");
        result.setRecords(list);
        
        return result;
    }

    @Override
    public MyCoursesResponse getMyCourses() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        MyCoursesResponse response = new MyCoursesResponse();
        
        // 1. 查询我教的课程（通过 teacherId 查询）
        List<Course> teachingCourses = courseService.list(new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, userId));
        
        List<MyCoursesResponse.MyCourseItem> teaching = teachingCourses.stream().map(course -> {
            MyCoursesResponse.MyCourseItem item = new MyCoursesResponse.MyCourseItem();
            item.setCourseId(course.getId());
            item.setCourseCode(course.getCourseCode());
            item.setCourseName(course.getCourseName());
            item.setCourseCover(course.getCourseCover());
            item.setSubjectArea(course.getSubjectArea());
            item.setMyRole(1); // 主讲教师
            item.setStudentCount(course.getStudentCount());
            item.setStatus(course.getStatus());
            return item;
        }).collect(Collectors.toList());
        
        // 2. 查询我加入的课程（通过 course_member 表）
        List<CourseMember> members = this.list(new LambdaQueryWrapper<CourseMember>()
                .eq(CourseMember::getUserId, userId)
                .eq(CourseMember::getJoinStatus, 1));
        
        List<MyCoursesResponse.MyCourseItem> learning = new ArrayList<>();
        List<MyCoursesResponse.MyCourseItem> assisting = new ArrayList<>();
        
        if (!members.isEmpty()) {
            List<Long> courseIds = members.stream().map(CourseMember::getCourseId).collect(Collectors.toList());
            List<Course> courses = courseService.listByIds(courseIds);
            
            for (Course course : courses) {
                CourseMember member = members.stream()
                        .filter(m -> m.getCourseId().equals(course.getId()))
                        .findFirst()
                        .orElse(null);
                
                if (member != null) {
                    MyCoursesResponse.MyCourseItem item = new MyCoursesResponse.MyCourseItem();
                    item.setCourseId(course.getId());
                    item.setCourseCode(course.getCourseCode());
                    item.setCourseName(course.getCourseName());
                    item.setCourseCover(course.getCourseCover());
                    item.setSubjectArea(course.getSubjectArea());
                    item.setMyRole(member.getMemberRole());
                    item.setStudentCount(course.getStudentCount());
                    item.setStatus(course.getStatus());
                    
                    if (member.getMemberRole() == 2) {
                        assisting.add(item);
                    } else if (member.getMemberRole() == 3) {
                        learning.add(item);
                    }
                    // 角色为1的已经在teaching中了，不重复添加
                }
            }
        }
        
        response.setTeaching(teaching);
        response.setLearning(learning);
        response.setAssisting(assisting);
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMember(Long courseId, AddMemberRequest request) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        // 权限校验：只有课程教师或管理员可以添加成员
        if (!hasManagePermission(courseId, currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权添加成员");
        }
        
        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "课程不存在");
        }
        
        // 检查是否已存在
        CourseMember exist = this.getOne(new LambdaQueryWrapper<CourseMember>()
                .eq(CourseMember::getCourseId, courseId)
                .eq(CourseMember::getUserId, request.getUserId()));
                
        if (exist != null && exist.getJoinStatus() == 1) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "该用户已是课程成员");
        }
        
        CourseMember member = (exist != null) ? exist : new CourseMember();
        member.setCourseId(courseId);
        member.setUserId(request.getUserId());
        member.setMemberRole(request.getMemberRole());
        member.setJoinStatus(1); // 直接加入
        member.setJoinTime(LocalDateTime.now());
        member.setApproveTime(LocalDateTime.now());
        member.setApproverId(currentUserId);
        
        this.saveOrUpdate(member);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long courseId, Long userId) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        // 权限校验：只有课程教师或管理员可以移除成员
        if (!hasManagePermission(courseId, currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权移除成员");
        }
        
        // 通过 courseId 和 userId 查询成员记录
        CourseMember member = this.getOne(new LambdaQueryWrapper<CourseMember>()
                .eq(CourseMember::getCourseId, courseId)
                .eq(CourseMember::getUserId, userId));
        
        if (member == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "成员记录不存在");
        }
        
        // 不能移除课程教师
        Course course = courseService.getById(courseId);
        if (course != null && course.getTeacherId().equals(member.getUserId())) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "不能移除课程教师");
        }
        
        // 逻辑删除：设置为已退出
        member.setJoinStatus(3);
        this.updateById(member);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberRole(Long courseId, Long userId, UpdateMemberRoleRequest request) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        // 权限校验：只有课程教师或管理员可以修改角色
        if (!hasManagePermission(courseId, currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权修改成员角色");
        }
        
        // 通过 courseId 和 userId 查询成员记录
        CourseMember member = this.getOne(new LambdaQueryWrapper<CourseMember>()
                .eq(CourseMember::getCourseId, courseId)
                .eq(CourseMember::getUserId, userId));
        
        if (member == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "成员记录不存在");
        }
        
        if (member.getJoinStatus() != 1) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "只能修改已加入成员的角色");
        }
        
        member.setMemberRole(request.getMemberRole());
        this.updateById(member);
    }
    
    /**
     * 检查是否有审批权限（教师、助教、管理员）
     */
    private boolean hasApprovePermission(Long courseId, Long userId) {
        // 管理员有权限
        if (PermissionUtil.isAdminOrLeader()) {
            return true;
        }
        
        // 检查是否是课程教师
        Course course = courseService.getById(courseId);
        if (course != null && course.getTeacherId().equals(userId)) {
            return true;
        }
        
        // 检查是否是助教
        CourseMember member = this.getOne(new LambdaQueryWrapper<CourseMember>()
                .eq(CourseMember::getCourseId, courseId)
                .eq(CourseMember::getUserId, userId)
                .eq(CourseMember::getJoinStatus, 1));
        
        return member != null && member.getMemberRole() == 2;
    }
    
    /**
     * 检查是否有管理权限（教师、管理员）
     */
    private boolean hasManagePermission(Long courseId, Long userId) {
        // 管理员有权限
        if (PermissionUtil.isAdminOrLeader()) {
            return true;
        }
        
        // 检查是否是课程教师
        Course course = courseService.getById(courseId);
        return course != null && course.getTeacherId().equals(userId);
    }
    
    @Override
    public boolean isCourseMember(Long courseId, Long userId) {
        LambdaQueryWrapper<CourseMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseMember::getCourseId, courseId)
               .eq(CourseMember::getUserId, userId)
               .eq(CourseMember::getJoinStatus, 1); // 1表示已加入
        return baseMapper.selectCount(wrapper) > 0;
    }
}

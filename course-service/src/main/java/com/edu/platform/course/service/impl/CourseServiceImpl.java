package com.edu.platform.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.course.dto.request.CourseAuditRequest;
import com.edu.platform.course.dto.request.CourseCreateRequest;
import com.edu.platform.course.dto.request.CourseQueryRequest;
import com.edu.platform.course.dto.request.CourseUpdateRequest;
import com.edu.platform.course.dto.response.CourseDetailResponse;
import com.edu.platform.course.dto.response.CourseListResponse;
import com.edu.platform.course.entity.Course;
import com.edu.platform.course.mapper.CourseMapper;
import com.edu.platform.course.service.CourseService;
import com.edu.platform.course.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCourse(CourseCreateRequest request) {
        // 1. 获取当前用户ID
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        // 2. 校验权限 (只有教师、校领导或管理员可以创建课程)
        if (!PermissionUtil.isTeacherOrAbove()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有教师可以创建课程");
        }

        Course course = new Course();
        BeanUtil.copyProperties(request, course);

        // 生成课程编码 (示例逻辑: CS + 时间戳 + 随机数)
        String courseCode = "CS" + System.currentTimeMillis() + RandomUtil.randomNumbers(3);
        course.setCourseCode(courseCode);

        // 设置当前用户为教师
        course.setTeacherId(currentUserId);

        // 设置默认状态
        course.setStudentCount(0);
        course.setCoursewareCount(0);
        course.setTaskCount(0);
        course.setDiscussionCount(0);
        course.setAuditStatus(0); // 待审核
        course.setStatus(1); // 默认开放

        this.save(course);
        return course.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(CourseUpdateRequest request) {
        Course course = this.getById(request.getId());
        if (course == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "课程不存在");
        }

        // 校验权限: 仅课程教师或管理员可修改
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
             throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        if (!PermissionUtil.hasCourseManagePermission(course.getTeacherId(), currentUserId)) {
             throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权修改此课程");
        }

        BeanUtil.copyProperties(request, course);
        this.updateById(course);
    }

    @Override
    public CourseDetailResponse getCourseDetail(Long id) {
        Course course = this.getById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "课程不存在");
        }
        
        Long currentUserId = UserContext.getUserId();
        
        // 权限校验：根据用户角色过滤
        boolean isAdmin = PermissionUtil.isAdminOrLeader();
        boolean isTeacher = course.getTeacherId().equals(currentUserId);
        
        // 普通用户只能看已审核且开放的课程
        if (!isAdmin && !isTeacher) {
            if (course.getAuditStatus() != 1 || course.getStatus() != 1) {
                throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "课程未开放或未审核");
            }
        }

        CourseDetailResponse response = new CourseDetailResponse();
        BeanUtil.copyProperties(course, response);

        // TODO: 远程调用获取 学校名称, 教师名称
        response.setSchoolName("Test School");
        response.setTeacherName("Test Teacher");
        response.setTeacherAvatar("");

        return response;
    }

    @Override
    public Page<CourseListResponse> pageCourses(CourseQueryRequest request) {
        Long currentUserId = UserContext.getUserId();
        boolean isAdmin = PermissionUtil.isAdminOrLeader();
        
        Page<Course> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StrUtil.isNotBlank(request.getKeyword()), Course::getCourseName, request.getKeyword())
                .eq(request.getSchoolId() != null, Course::getSchoolId, request.getSchoolId())
                .eq(StrUtil.isNotBlank(request.getSubjectArea()), Course::getSubjectArea, request.getSubjectArea())
                .eq(request.getJoinType() != null, Course::getJoinType, request.getJoinType())
                .eq(request.getStatus() != null, Course::getStatus, request.getStatus())
                .eq(request.getAuditStatus() != null, Course::getAuditStatus, request.getAuditStatus())
                .eq(request.getTeacherId() != null, Course::getTeacherId, request.getTeacherId());
        
        // 数据过滤：普通用户只能看已审核且开放的课程
        if (!isAdmin) {
            // 如果是教师查询自己的课程，不过滤
            if (request.getTeacherId() == null || !request.getTeacherId().equals(currentUserId)) {
                // 普通用户只能看已审核且开放的课程
                wrapper.eq(Course::getAuditStatus, 1)
                       .eq(Course::getStatus, 1);
            }
        }
        
        wrapper.orderByDesc(Course::getCreatedTime);

        Page<Course> coursePage = this.page(page, wrapper);

        // 转换 Response
        List<CourseListResponse> list = coursePage.getRecords().stream().map(course -> {
            CourseListResponse resp = new CourseListResponse();
            BeanUtil.copyProperties(course, resp);
            // TODO: 填充名称
            resp.setSchoolName("Test School");
            resp.setTeacherName("Test Teacher");
            return resp;
        }).collect(Collectors.toList());

        Page<CourseListResponse> resultPage = new Page<>();
        BeanUtil.copyProperties(coursePage, resultPage, "records");
        resultPage.setRecords(list);

        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        Course course = this.getById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "课程不存在");
        }
        
        // 权限校验：只有课程教师、校领导或管理员可以修改状态
        if (!PermissionUtil.hasCourseManagePermission(course.getTeacherId(), currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权修改课程状态");
        }
        
        course.setStatus(status);
        this.updateById(course);
    }
    
    @Override
    public Page<CourseListResponse> getPendingCourses(CourseQueryRequest request) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        // 权限校验：只有管理员或校领导可以查看待审核课程
        if (!PermissionUtil.isAdminOrLeader()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看待审核课程");
        }
        
        Page<Course> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询待审核的课程
        wrapper.eq(Course::getAuditStatus, 0)
                .like(StrUtil.isNotBlank(request.getKeyword()), Course::getCourseName, request.getKeyword())
                .eq(request.getSchoolId() != null, Course::getSchoolId, request.getSchoolId())
                .eq(StrUtil.isNotBlank(request.getSubjectArea()), Course::getSubjectArea, request.getSubjectArea())
                .orderByDesc(Course::getCreatedTime);
        
        Page<Course> coursePage = this.page(page, wrapper);
        
        List<CourseListResponse> list = coursePage.getRecords().stream().map(course -> {
            CourseListResponse resp = new CourseListResponse();
            BeanUtil.copyProperties(course, resp);
            resp.setSchoolName("Test School");
            resp.setTeacherName("Test Teacher");
            return resp;
        }).collect(Collectors.toList());
        
        Page<CourseListResponse> resultPage = new Page<>();
        BeanUtil.copyProperties(coursePage, resultPage, "records");
        resultPage.setRecords(list);
        
        return resultPage;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditCourse(Long id, CourseAuditRequest request) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        // 权限校验：只有管理员或校领导可以审核课程
        if (!PermissionUtil.isAdminOrLeader()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权审核课程");
        }
        
        Course course = this.getById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "课程不存在");
        }
        
        if (course.getAuditStatus() != 0) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "该课程已审核，无需重复审核");
        }
        
        // 设置审核结果
        course.setAuditStatus(request.getApproved() ? 1 : 2); // 1=通过, 2=拒绝
        // TODO: 记录审核备注和审核人（需要添加字段）
        
        this.updateById(course);
    }
}

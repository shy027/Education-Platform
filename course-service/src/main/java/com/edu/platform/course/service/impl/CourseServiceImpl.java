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
import com.edu.platform.course.service.SubjectCategoryService;
import com.edu.platform.course.client.UserServiceClient;
import com.edu.platform.course.dto.UserInfoDTO;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final SubjectCategoryService subjectCategoryService;
    private final UserServiceClient userServiceClient;

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
        
        // 3. 校验学科领域是否存在且启用
        if (StrUtil.isNotBlank(request.getSubjectArea())) {
            boolean exists = subjectCategoryService.lambdaQuery()
                    .eq(com.edu.platform.course.entity.SubjectCategory::getName, request.getSubjectArea())
                    .eq(com.edu.platform.course.entity.SubjectCategory::getIsEnabled, 1)
                    .exists();
            if (!exists) {
                throw new BusinessException(ResultCode.FAIL.getCode(), "学科领域不存在或未启用");
            }
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
        
        // 普通用户只能看已审核且开放或已结课的课程
        if (!isAdmin && !isTeacher) {
            if (course.getAuditStatus() != 1 || (course.getStatus() != 1 && course.getStatus() != 2)) {
                throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "课程未开放或未审核");
            }
        }

        CourseDetailResponse response = new CourseDetailResponse();
        BeanUtil.copyProperties(course, response);

        try {
            Result<Map<Long, UserInfoDTO>> userResult = userServiceClient.batchGetUserInfo(Collections.singletonList(course.getTeacherId()));
            if (userResult.isSuccess() && userResult.getData() != null && userResult.getData().containsKey(course.getTeacherId())) {
                UserInfoDTO teacher = userResult.getData().get(course.getTeacherId());
                response.setTeacherName(StrUtil.isNotBlank(teacher.getRealName()) ? teacher.getRealName() : teacher.getUsername());
            } else {
                response.setTeacherName("未知教师");
            }
        } catch (Exception e) {
            log.error("获取教师信息失败: {}", e.getMessage());
            response.setTeacherName("未知教师");
        }

        // TODO: 远程调用获取 学校名称
        response.setSchoolName("Test School");
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
                .eq(request.getAuditStatus() != null, Course::getAuditStatus, request.getAuditStatus())
                .eq(request.getTeacherId() != null, Course::getTeacherId, request.getTeacherId());
        
        // 处理前端传入的基于时间的计算状态 (0:未开放, 1:进行中, 2:已结课)
        // 注意前端此时借用了 status 字段传递这个时间维度的查询诉求
        if (request.getStatus() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (request.getStatus() == 0) {
                // 暂未开放: startTime > now
                wrapper.gt(Course::getStartTime, now);
            } else if (request.getStatus() == 1) {
                // 进行中: (startTime <= now or null) AND (endTime > now or null)
                wrapper.and(w -> w.isNull(Course::getStartTime).or().le(Course::getStartTime, now))
                       .and(w -> w.isNull(Course::getEndTime).or().gt(Course::getEndTime, now));
            } else if (request.getStatus() == 2) {
                // 已结课: endTime <= now
                wrapper.le(Course::getEndTime, now);
            }
        }
        
        // 数据过滤：普通用户只能看已审核，且原状态为 1(开放) 或 2(归档) 的课程
        if (!isAdmin) {
            // 如果是教师查询自己的课程，不过滤
            if (request.getTeacherId() == null || !request.getTeacherId().equals(currentUserId)) {
                wrapper.eq(Course::getAuditStatus, 1)
                       .in(Course::getStatus, Arrays.asList(1, 2));
            }
        }
        
        wrapper.orderByDesc(Course::getCreatedTime);

        Page<Course> coursePage = this.page(page, wrapper);

        // 批量获取教师信息
        List<Long> teacherIds = coursePage.getRecords().stream()
                .map(Course::getTeacherId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, UserInfoDTO> userMap = null;
        if (!teacherIds.isEmpty()) {
            try {
                Result<Map<Long, UserInfoDTO>> userResult = userServiceClient.batchGetUserInfo(teacherIds);
                if (userResult.isSuccess()) {
                    userMap = userResult.getData();
                }
            } catch (Exception e) {
                log.error("批量获取教师信息失败: {}", e.getMessage());
            }
        }
        
        final Map<Long, UserInfoDTO> finalUserMap = userMap;

        // 转换 Response
        List<CourseListResponse> list = coursePage.getRecords().stream().map(course -> {
            CourseListResponse resp = new CourseListResponse();
            BeanUtil.copyProperties(course, resp);
            // 前端存在字段名称差异，需手动映射
            resp.setCover(course.getCourseCover());
            resp.setDescription(course.getCourseIntro());
            resp.setMemberCount(course.getStudentCount());
            
            if (finalUserMap != null && finalUserMap.containsKey(course.getTeacherId())) {
                UserInfoDTO teacher = finalUserMap.get(course.getTeacherId());
                resp.setTeacherName(StrUtil.isNotBlank(teacher.getRealName()) ? teacher.getRealName() : teacher.getUsername());
            } else {
                resp.setTeacherName("未知教师");
            }
            // TODO: 填充学校名称
            resp.setSchoolName("Test School");
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
        
        // 批量获取教师信息
        List<Long> teacherIds = coursePage.getRecords().stream()
                .map(Course::getTeacherId)
                .distinct()
                .collect(Collectors.toList());
                
        Map<Long, UserInfoDTO> userMap = null;
        if (!teacherIds.isEmpty()) {
            try {
                Result<Map<Long, UserInfoDTO>> userResult = userServiceClient.batchGetUserInfo(teacherIds);
                if (userResult.isSuccess()) {
                    userMap = userResult.getData();
                }
            } catch (Exception e) {
                log.error("批量获取教师信息失败: {}", e.getMessage());
            }
        }
        final Map<Long, UserInfoDTO> finalUserMap = userMap;
        
        List<CourseListResponse> list = coursePage.getRecords().stream().map(course -> {
            CourseListResponse resp = new CourseListResponse();
            BeanUtil.copyProperties(course, resp);
            resp.setCover(course.getCourseCover());
            resp.setDescription(course.getCourseIntro());
            resp.setMemberCount(course.getStudentCount());
            resp.setSchoolName("Test School");
            
            if (finalUserMap != null && finalUserMap.containsKey(course.getTeacherId())) {
                UserInfoDTO teacher = finalUserMap.get(course.getTeacherId());
                resp.setTeacherName(StrUtil.isNotBlank(teacher.getRealName()) ? teacher.getRealName() : teacher.getUsername());
            } else {
                resp.setTeacherName("未知教师");
            }
            
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

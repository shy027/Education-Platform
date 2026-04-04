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
    private final com.edu.platform.course.client.AuditClient auditClient;

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
        course.setAuditStatus(-1); // 草稿
        course.setStatus(0); // 默认关闭 (待审核通过后视时间开启)
        course.setIsDimensionLocked(0); // 初始未锁定

        // 保存维度权重和评分配置
        course.setDimensionWeights(request.getDimensionWeights());
        course.setScoringConfig(request.getScoringConfig());

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

        // 提交审核后锁定逻辑: 若 auditStatus 为 0(待审核) 或 1(通过)，非管理员不可修改基本信息
        boolean isAdmin = PermissionUtil.isAdminOrLeader();
        if (!isAdmin && (course.getAuditStatus() == 0 || course.getAuditStatus() == 1)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "课程已提交审核或已通过，基本信息已锁定");
        }

        // 校验维度锁定逻辑
        if (course.getIsDimensionLocked() == 1 && !isAdmin) {
            // 如果已锁定且不是管理员，禁止修改维度权重和锁定状态
            if (request.getDimensionWeights() != null && !request.getDimensionWeights().equals(course.getDimensionWeights())) {
                throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "维度定义已锁定，仅管理员可修改");
            }
            // 防止教师通过 update 接口自己解锁
            request.setIsDimensionLocked(1); 
        }

        BeanUtil.copyProperties(request, course, "id", "teacherId", "courseCode");
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
                UserInfoDTO user = finalUserMap.get(course.getTeacherId());
                resp.setTeacherName(StrUtil.isNotBlank(user.getRealName()) ? user.getRealName() : user.getUsername());
                resp.setSchoolName(user.getSchoolName() != null ? user.getSchoolName() : "未知院校");
            } else {
                resp.setTeacherName("未知教师");
                resp.setSchoolName("未知院校");
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
        boolean isApproved = request.getApproved();
        course.setAuditStatus(isApproved ? 1 : 2); // 1=通过, 2=拒绝
        
        // 如果审核通过，自动将课程状态设为“开放” (1)
        if (isApproved) {
            course.setStatus(1);
        }
        
        // 记录审核详细信息
        course.setAuditorId(currentUserId);
        course.setAuditTime(java.time.LocalDateTime.now());
        course.setAuditRemark(request.getAuditRemark());
        
        this.updateById(course);

        // 同步到中央审核中心
        try {
            java.util.Map<String, Object> auditRecord = new java.util.HashMap<>();
            auditRecord.put("contentType", "COURSE");
            auditRecord.put("contentId", id);
            auditRecord.put("auditResult", isApproved ? 1 : 2);
            auditRecord.put("auditReason", request.getAuditRemark());
            auditRecord.put("auditorId", currentUserId);
            auditClient.recordManualAudit(auditRecord);
        } catch (Exception e) {
            log.error("同步审核结果到审核中心失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForReview(Long id) {
        Course course = this.getById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "课程不存在");
        }
        
        Long currentUserId = UserContext.getUserId();
        if (!PermissionUtil.hasCourseManagePermission(course.getTeacherId(), currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权修改此课程");
        }

        if (course.getAuditStatus() != -1 && course.getAuditStatus() != 2) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "该课程状态不可重复提交审核");
        }

        if (StrUtil.isBlank(course.getCourseName())) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "课程名称不能为空，请先完善基本信息");
        }

        course.setAuditStatus(0); // 待审核
        this.updateById(course);

        // 同步到中央审核中心
        try {
            auditClient.submitAuditRequest("COURSE", id);
        } catch (Exception e) {
            log.error("提交课程审核申请到审核中心失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDraft(Long id) {
        Course course = this.getById(id);
        if (course == null) {
            return;
        }

        Long currentUserId = UserContext.getUserId();
        if (!PermissionUtil.hasCourseManagePermission(course.getTeacherId(), currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权删除此课程");
        }

        if (course.getAuditStatus() != -1 && course.getAuditStatus() != 2) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "非草稿或已拒绝状态的课程不可删除");
        }

        this.removeById(id); // MyBatis Plus 开启了逻辑删除
    }

    @Override
    public java.util.Map<String, Object> getCourseStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // 已通过审核（上架）的课程总数
        Long totalCourses = this.count(new LambdaQueryWrapper<Course>()
                .eq(Course::getAuditStatus, 1));
        stats.put("totalCourses", totalCourses);

        // 待审核课程数（直接从 course 表查，不依赖 audit_record）
        Long pendingCourses = this.count(new LambdaQueryWrapper<Course>()
                .eq(Course::getAuditStatus, 0));
        stats.put("pendingCourses", pendingCourses);
        
        // 学科课程分布（查全列避免 MyBatis-Plus 单列查询返回 null 实体问题）
        List<Course> allCourses = this.list(new LambdaQueryWrapper<Course>());
        java.util.Map<String, Long> distribution = allCourses.stream()
                .filter(c -> c != null)
                .collect(Collectors.groupingBy(
                    c -> cn.hutool.core.util.StrUtil.isNotBlank(c.getSubjectArea()) ? c.getSubjectArea() : "未分类",
                    Collectors.counting()
                ));
        
        stats.put("subjectDistribution", distribution);
        
        return stats;
    }

    @Override
    public java.util.List<com.edu.platform.course.dto.response.CourseListResponse> getPublishedCourses() {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getAuditStatus, 1);
        List<Course> courses = this.list(wrapper);
        
        return courses.stream().map(course -> {
            com.edu.platform.course.dto.response.CourseListResponse resp = new com.edu.platform.course.dto.response.CourseListResponse();
            cn.hutool.core.bean.BeanUtil.copyProperties(course, resp);
            resp.setCover(course.getCourseCover());
            resp.setDescription(course.getCourseIntro());
            resp.setMemberCount(course.getStudentCount());
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseAuditStatus(Long courseId, Integer auditStatus) {
        Course course = this.getById(courseId);
        if (course == null) {
            log.warn("审核回调：课程不存在, courseId={}", courseId);
            return;
        }
        course.setAuditStatus(auditStatus);
        this.updateById(course);
        log.info("课程审核状态已更新(内部回调): courseId={}, auditStatus={}", courseId, auditStatus);
    }
}

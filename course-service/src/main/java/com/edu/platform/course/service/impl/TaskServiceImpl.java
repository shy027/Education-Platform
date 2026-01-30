package com.edu.platform.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.course.dto.request.TaskCreateRequest;
import com.edu.platform.course.dto.request.TaskQueryRequest;
import com.edu.platform.course.dto.request.TaskUpdateRequest;
import com.edu.platform.course.dto.response.TaskResponse;
import com.edu.platform.course.entity.Course;
import com.edu.platform.course.entity.CourseTask;
import com.edu.platform.course.mapper.CourseTaskMapper;
import com.edu.platform.course.service.CourseService;
import com.edu.platform.course.service.TaskService;
import com.edu.platform.course.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends ServiceImpl<CourseTaskMapper, CourseTask> implements TaskService {

    private final CourseService courseService;
    
    private static final Map<Integer, String> TASK_TYPE_MAP = new HashMap<>();
    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();
    
    static {
        TASK_TYPE_MAP.put(1, "作业");
        TASK_TYPE_MAP.put(2, "测验");
        TASK_TYPE_MAP.put(3, "考试");
        
        STATUS_MAP.put(0, "草稿");
        STATUS_MAP.put(1, "发布");
        STATUS_MAP.put(2, "关闭");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(TaskCreateRequest request) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        // 检查课程是否存在
        Course course = courseService.getById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "课程不存在");
        }
        
        // 权限校验：只有课程教师或管理员可以创建任务
        if (!hasManagePermission(request.getCourseId(), currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权创建任务");
        }
        
        // 业务校验：如果设置了结束时间，结束时间必须晚于开始时间
        if (request.getEndTime() != null && request.getEndTime().isBefore(request.getStartTime())) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "结束时间必须晚于开始时间");
        }
        
        
        CourseTask task = new CourseTask();
        BeanUtil.copyProperties(request, task);
        task.setCreatorId(currentUserId);
        task.setSubmitCount(0);
        task.setStatus(0); // 默认草稿状态
        
        this.save(task);
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(TaskUpdateRequest request) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        CourseTask task = this.getById(request.getId());
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "任务不存在");
        }
        
        // 权限校验：只有课程教师或管理员可以更新
        if (!hasManagePermission(task.getCourseId(), currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权修改此任务");
        }
        
        // 业务校验：已发布的任务不能修改某些字段
        if (task.getStatus() == 1 && task.getSubmitCount() > 0) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "已有学生提交，无法修改任务");
        }
        
        // 业务校验：时间校验
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getEndTime().isBefore(request.getStartTime())) {
                throw new BusinessException(ResultCode.FAIL.getCode(), "结束时间必须晚于开始时间");
            }
        }
        
        if (StrUtil.isNotBlank(request.getTaskTitle())) {
            task.setTaskTitle(request.getTaskTitle());
        }
        if (StrUtil.isNotBlank(request.getTaskDescription())) {
            task.setTaskDescription(request.getTaskDescription());
        }
        if (request.getTotalScore() != null) {
            task.setTotalScore(request.getTotalScore());
        }
        if (request.getPassScore() != null) {
            task.setPassScore(request.getPassScore());
        }
        if (request.getStartTime() != null) {
            task.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            task.setEndTime(request.getEndTime());
        }
        if (request.getDurationMinutes() != null) {
            task.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getAllowRetry() != null) {
            task.setAllowRetry(request.getAllowRetry());
        }
        if (request.getMaxRetryTimes() != null) {
            task.setMaxRetryTimes(request.getMaxRetryTimes());
        }
        if (request.getShowAnswer() != null) {
            task.setShowAnswer(request.getShowAnswer());
        }
        if (request.getRandomQuestion() != null) {
            task.setRandomQuestion(request.getRandomQuestion());
        }
        
        this.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long courseId, Long id) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        CourseTask task = this.getById(id);
        if (task == null || !task.getCourseId().equals(courseId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "任务不存在");
        }
        
        // 权限校验：只有课程教师或管理员可以删除
        if (!hasManagePermission(courseId, currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权删除此任务");
        }
        
        // 业务校验：已有提交的任务不能删除
        if (task.getSubmitCount() > 0) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "已有学生提交，无法删除任务");
        }
        
        this.removeById(id);
    }

    @Override
    public TaskResponse getTaskDetail(Long courseId, Long id) {
        CourseTask task = this.getById(id);
        if (task == null || !task.getCourseId().equals(courseId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "任务不存在");
        }
        
        Long currentUserId = UserContext.getUserId();
        
        // 权限校验：未发布的任务只有教师可以查看
        if (task.getStatus() == 0 && !hasManagePermission(courseId, currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "任务未发布");
        }
        
        // 业务校验：学生只能看到开始时间已到的任务
        if (!hasManagePermission(courseId, currentUserId)) {
            if (task.getStartTime().isAfter(LocalDateTime.now())) {
                throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "任务未开始");
            }
        }
        
        return convertToResponse(task);
    }

    @Override
    public Page<TaskResponse> pageTasks(TaskQueryRequest request) {
        Long currentUserId = UserContext.getUserId();
        boolean isTeacher = hasManagePermission(request.getCourseId(), currentUserId);
        
        Page<CourseTask> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<CourseTask> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(request.getCourseId() != null, CourseTask::getCourseId, request.getCourseId())
                .like(StrUtil.isNotBlank(request.getKeyword()), CourseTask::getTaskTitle, request.getKeyword())
                .eq(request.getTaskType() != null, CourseTask::getTaskType, request.getTaskType())
                .eq(request.getStatus() != null, CourseTask::getStatus, request.getStatus());
        
        // 学生只能看已发布且开始时间已到的任务
        if (!isTeacher) {
            wrapper.eq(CourseTask::getStatus, 1)
                   .le(CourseTask::getStartTime, LocalDateTime.now());
        }
        
        wrapper.orderByDesc(CourseTask::getCreatedTime);
        
        Page<CourseTask> taskPage = this.page(page, wrapper);
        
        List<TaskResponse> list = taskPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        Page<TaskResponse> resultPage = new Page<>();
        BeanUtil.copyProperties(taskPage, resultPage, "records");
        resultPage.setRecords(list);
        
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long courseId, Long id, Integer status) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        CourseTask task = this.getById(id);
        if (task == null || !task.getCourseId().equals(courseId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "任务不存在");
        }
        
        // 权限校验：只有课程教师或管理员可以修改状态
        if (!hasManagePermission(courseId, currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权修改任务状态");
        }
        
        // 业务校验：状态值必须有效
        if (status < 0 || status > 2) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "无效的状态值");
        }
        
        task.setStatus(status);
        this.updateById(task);
    }
    
    /**
     * 转换为响应对象
     */
    private TaskResponse convertToResponse(CourseTask task) {
        TaskResponse response = new TaskResponse();
        BeanUtil.copyProperties(task, response);
        
        // 设置类型名称
        response.setTaskTypeName(TASK_TYPE_MAP.getOrDefault(task.getTaskType(), "未知"));
        
        // 设置状态名称
        response.setStatusName(STATUS_MAP.getOrDefault(task.getStatus(), "未知"));
        
        // TODO: 远程调用获取创建者姓名
        response.setCreatorName("创建者");
        
        return response;
    }
    
    /**
     * 检查是否有管理权限（教师、校领导、管理员）
     */
    private boolean hasManagePermission(Long courseId, Long userId) {
        if (userId == null) {
            return false;
        }
        
        if (PermissionUtil.isAdminOrLeader()) {
            return true;
        }
        
        Course course = courseService.getById(courseId);
        return course != null && course.getTeacherId().equals(userId);
    }
}

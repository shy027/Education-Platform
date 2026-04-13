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
import com.edu.platform.course.entity.ExamRecord;
import com.edu.platform.course.mapper.CourseTaskMapper;
import com.edu.platform.course.mapper.ExamRecordMapper;
import com.edu.platform.course.service.CourseService;
import com.edu.platform.course.service.TaskService;
import com.edu.platform.course.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final ExamRecordMapper recordMapper;
    
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
        
        // 业务校验：如果为测验或考试，必须要有起止时间
        if ((request.getTaskType() == 2 || request.getTaskType() == 3) 
            && (request.getStartTime() == null || request.getEndTime() == null)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "测验和考试必须设置开始时间和截止时间");
        }
        
        // 业务校验：如果设置了结束时间，结束时间必须晚于开始时间
        if (request.getEndTime() != null && request.getEndTime().isBefore(request.getStartTime())) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "结束时间必须晚于开始时间");
        }
        
        
        CourseTask task = new CourseTask();
        BeanUtil.copyProperties(request, task);
        task.setCreatorId(currentUserId);
        task.setSubmitCount(0);
        
        // 默认所有类型均为草稿状态(0)，需进入组卷/检查后发布
        if (task.getStatus() == null) {
            task.setStatus(0);
        }
        
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
        
        TaskResponse response = convertToResponse(task);
        
        // 如果是学生，尝试获取其答题记录状态
        if (currentUserId != null && !hasManagePermission(courseId, currentUserId)) {
            List<ExamRecord> records = recordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                    .eq(ExamRecord::getTaskId, id)
                    .eq(ExamRecord::getUserId, currentUserId)
                    .orderByDesc(ExamRecord::getCreatedTime));
            
            if (!records.isEmpty()) {
                // 1. 尝试找到进行中的记录
                ExamRecord inProgress = records.stream()
                        .filter(r -> r.getStatus() == 0)
                        .findFirst().orElse(null);
                if (inProgress != null) {
                    response.setInProgressId(inProgress.getId());
                    response.setStudentRecordId(inProgress.getId());
                    response.setStudentStatus(0);
                } else {
                    // 2. 如果没有进行中，取最高分的或者最近的一条显示状态
                    ExamRecord latest = records.get(0);
                    response.setStudentRecordId(latest.getId());
                    response.setStudentStatus(latest.getStatus());
                    response.setStudentScore(latest.getTotalScore());
                }
                
                // 3. 统计已尝试次数 (已提交或已批改)
                long attemptCount = records.stream().filter(r -> r.getStatus() != 0).count();
                response.setAttemptCount((int) attemptCount);
            }
        }
        
        return response;
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
        
        // 获取分页记录
        List<CourseTask> records = taskPage.getRecords();
        if (records.isEmpty()) {
            return new Page<TaskResponse>().setRecords(new ArrayList<>());
        }

        // 批量查询当前学生的答题记录（如果是学生视角）
        Map<Long, List<ExamRecord>> studentRecordGroup = new HashMap<>();
        if (currentUserId != null && !isTeacher) {
            List<Long> taskIds = records.stream().map(CourseTask::getId).collect(Collectors.toList());
            List<ExamRecord> recordList = recordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                    .in(ExamRecord::getTaskId, taskIds)
                    .eq(ExamRecord::getUserId, currentUserId)
                    .orderByDesc(ExamRecord::getCreatedTime));
            
            // 按任务ID分组
            studentRecordGroup = recordList.stream().collect(Collectors.groupingBy(ExamRecord::getTaskId));
        }
    
        Map<Long, List<ExamRecord>> finalRecordGroup = studentRecordGroup;
        List<TaskResponse> list = records.stream()
                .map(this::convertToResponse)
                .peek(resp -> {
                    List<ExamRecord> sRecords = finalRecordGroup.get(resp.getId());
                    if (sRecords != null && !sRecords.isEmpty()) {
                        // 1. 查找进行中的记录
                        ExamRecord inProgress = sRecords.stream()
                                .filter(r -> r.getStatus() == 0)
                                .findFirst().orElse(null);
                                
                        if (inProgress != null) {
                            resp.setInProgressId(inProgress.getId());
                            resp.setStudentRecordId(inProgress.getId());
                            resp.setStudentStatus(0);
                        } else {
                            // 2. 没有进行中的，显示最新的一条记录状态
                            ExamRecord latest = sRecords.get(0);
                            resp.setStudentRecordId(latest.getId());
                            resp.setStudentStatus(latest.getStatus());
                            resp.setStudentScore(latest.getTotalScore());
                        }
                        
                        // 3. 计算已满负荷尝试次数 (status != 0)
                        long attemptCount = sRecords.stream().filter(r -> r.getStatus() != 0).count();
                        resp.setAttemptCount((int) attemptCount);

                        // 4. 提取所有已批改的记录 (status == 2)
                        List<TaskResponse.GradedRecord> graded = sRecords.stream()
                                .filter(r -> r.getStatus() == 2)
                                .map(r -> {
                                    TaskResponse.GradedRecord gr = new TaskResponse.GradedRecord();
                                    gr.setRecordId(r.getId());
                                    gr.setScore(r.getTotalScore());
                                    gr.setSubmitTime(r.getSubmitTime());
                                    return gr;
                                })
                                .sorted((a, b) -> {
                                    if (a.getScore() != null && b.getScore() != null) {
                                        int cmp = b.getScore().compareTo(a.getScore());
                                        if (cmp != 0) return cmp;
                                    }
                                    return b.getSubmitTime().compareTo(a.getSubmitTime());
                                })
                                .collect(Collectors.toList());
                        resp.setGradedRecords(graded);
                    }
                })
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

        // 计算考试时间状态 (0-未开始 1-进行中 2-已结束)
        LocalDateTime now = LocalDateTime.now();
        if (task.getStartTime() != null && now.isBefore(task.getStartTime())) {
            response.setExamStatus(0);
        } else if (task.getEndTime() != null && now.isAfter(task.getEndTime())) {
            response.setExamStatus(2);
        } else {
            response.setExamStatus(1);
        }
        
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

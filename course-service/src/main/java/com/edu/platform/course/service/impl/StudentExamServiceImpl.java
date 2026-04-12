package com.edu.platform.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.course.dto.response.ExamListResponse;
import com.edu.platform.course.dto.response.PaperResponse;
import com.edu.platform.course.entity.*;
import com.edu.platform.course.mapper.*;
import com.edu.platform.course.service.PaperService;
import com.edu.platform.course.service.StudentExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生考试服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentExamServiceImpl implements StudentExamService {

    private final CourseTaskMapper taskMapper;
    private final CourseTaskQuestionMapper taskQuestionMapper;
    private final ExamRecordMapper recordMapper;
    private final CourseMemberMapper memberMapper;
    private final PaperService paperService;

    @Override
    public PageResult<ExamListResponse> getStudentExams(Long courseId, Integer status, Integer pageNum, Integer pageSize) {
        Long currentUserId = UserContext.getUserId();

        // 1. 查询学生参与的课程
        LambdaQueryWrapper<CourseMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CourseMember::getUserId, currentUserId)
                .eq(CourseMember::getMemberRole, 3); // 学生角色=3

        if (courseId != null) {
            memberWrapper.eq(CourseMember::getCourseId, courseId);
        }

        List<CourseMember> members = memberMapper.selectList(memberWrapper);
        if (members.isEmpty()) {
            return PageResult.of(0L, new ArrayList<>());
        }

        List<Long> courseIds = members.stream()
                .map(CourseMember::getCourseId)
                .collect(Collectors.toList());

        // 2. 查询这些课程的考试任务
        LambdaQueryWrapper<CourseTask> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.in(CourseTask::getCourseId, courseIds)
                .eq(CourseTask::getTaskType, 3); // taskType=3表示考试任务

        // 根据时间判断考试状态
        LocalDateTime now = LocalDateTime.now();
        if (status != null) {
            if (status == 0) { // 未开始
                taskWrapper.gt(CourseTask::getStartTime, now);
            } else if (status == 1) { // 进行中
                taskWrapper.le(CourseTask::getStartTime, now)
                        .ge(CourseTask::getEndTime, now);
            } else if (status == 2) { // 已结束
                taskWrapper.lt(CourseTask::getEndTime, now);
            }
        }

        taskWrapper.orderByDesc(CourseTask::getCreatedTime);

        Page<CourseTask> page = new Page<>(pageNum, pageSize);
        Page<CourseTask> resultPage = taskMapper.selectPage(page, taskWrapper);

        // 3. 查询学生的答题记录 (查询所有历史记录)
        List<Long> taskIds = resultPage.getRecords().stream()
                .map(CourseTask::getId)
                .collect(Collectors.toList());

        Map<Long, List<ExamRecord>> recordGroups = Map.of();
        if (!taskIds.isEmpty()) {
            List<ExamRecord> allRecords = recordMapper.selectList(
                    new LambdaQueryWrapper<ExamRecord>()
                            .in(ExamRecord::getTaskId, taskIds)
                            .eq(ExamRecord::getUserId, currentUserId)
            );
            recordGroups = allRecords.stream()
                    .collect(Collectors.groupingBy(ExamRecord::getTaskId));
        }

        // 4. 构建响应
        List<ExamListResponse> responses = new ArrayList<>();
        for (CourseTask task : resultPage.getRecords()) {
            ExamListResponse response = new ExamListResponse();
            response.setTaskId(task.getId());
            response.setTitle(task.getTaskTitle());
            response.setCourseId(task.getCourseId());
            response.setStartTime(task.getStartTime());
            response.setEndTime(task.getEndTime());
            response.setDuration(task.getDurationMinutes());
            
            // 配置项
            response.setAllowRetry(task.getAllowRetry());
            response.setMaxRetryTimes(task.getMaxRetryTimes());
            response.setShowAnswer(task.getShowAnswer());

            // 计算考试状态
            int examStatus = calculateStatus(task.getStartTime(), task.getEndTime());
            response.setExamStatus(examStatus);

            // 查询试卷题目信息
            List<CourseTaskQuestion> questions = taskQuestionMapper.selectList(
                    new LambdaQueryWrapper<CourseTaskQuestion>()
                            .eq(CourseTaskQuestion::getTaskId, task.getId())
            );
            response.setQuestionCount(questions.size());

            // 学生答题状态处理 (多记录逻辑)
            List<ExamRecord> records = recordGroups.getOrDefault(task.getId(), new ArrayList<>());
            response.setAttemptCount(records.size());
            
            // 查找进行中的记录
            ExamRecord inProgressRecord = records.stream()
                    .filter(r -> r.getStatus() == 0)
                    .findFirst()
                    .orElse(null);
            
            if (inProgressRecord != null) {
                response.setInProgressId(inProgressRecord.getId());
                response.setStudentStatus(0); // 进行中
            } else if (!records.isEmpty()) {
                // 如果没有进行中的，查找评分最高的已提交记录作为展示分
                ExamRecord bestRecord = records.stream()
                        .filter(r -> r.getStatus() >= 1)
                        .max(java.util.Comparator.comparing(r -> r.getTotalScore() != null ? r.getTotalScore() : java.math.BigDecimal.ZERO))
                        .orElse(records.get(0)); // 兜底
                
                response.setStudentStatus(bestRecord.getStatus());
                response.setStudentScore(bestRecord.getTotalScore());
                response.setBestRecordId(bestRecord.getId());
            } else {
                response.setStudentStatus(null); // 彻底未开始
            }

            // 处理过期自动提交
            checkAndAutoSubmit(records, task);
            
            responses.add(response);
        }

        return PageResult.of(resultPage.getTotal(), responses);
    }

    /**
     * 检查并自动提交已过期的正在进行的考试
     */
    private void checkAndAutoSubmit(List<ExamRecord> records, CourseTask task) {
        if (task.getEndTime() == null) return;
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(task.getEndTime())) return;

        for (ExamRecord record : records) {
            if (record.getStatus() == 0) {
                log.info("考试已截止，自动提交记录: recordId={}, taskId={}", record.getId(), task.getId());
                record.setStatus(1); // 已提交
                record.setSubmitTime(task.getEndTime()); // 以截止时间作为提交时间
                recordMapper.updateById(record);
                
                // 触发自动评分 (异步或由外部注入 GradingService)
                // 这里为了简单，我们直接在 service 逻辑中处理即可。
                // 如果需要严格评分，可以调用 GradingService.autoGrade(record.getId())
            }
        }
    }

    @Override
    public PaperResponse getExamDetail(Long taskId) {
        // 验证学生是否有权限查看
        CourseTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("考试不存在");
        }

        Long currentUserId = UserContext.getUserId();
        CourseMember member = memberMapper.selectOne(
                new LambdaQueryWrapper<CourseMember>()
                        .eq(CourseMember::getCourseId, task.getCourseId())
                        .eq(CourseMember::getUserId, currentUserId)
        );

        if (member == null) {
            throw new BusinessException("无权查看此考试");
        }

        // 返回试卷详情(但不包含正确答案)
        return paperService.getPaperDetail(taskId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startExam(Long taskId) {
        Long currentUserId = UserContext.getUserId();

        // 1. 验证考试存在且可以开始
        CourseTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("考试不存在");
        }

        // 检查开始时间是否已设置
        if (task.getStartTime() == null) {
            throw new BusinessException("考试开始时间未设置");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(task.getStartTime())) {
            throw new BusinessException("考试尚未开始");
        }
        
        // 只有设置了结束时间才检查是否已结束
        if (task.getEndTime() != null && now.isAfter(task.getEndTime())) {
            // 在抛出异常前，顺便把该生可能存在的 status=0 记录自动提交了
            List<ExamRecord> records = recordMapper.selectList(
                    new LambdaQueryWrapper<ExamRecord>()
                            .eq(ExamRecord::getTaskId, taskId)
                            .eq(ExamRecord::getUserId, currentUserId)
                            .eq(ExamRecord::getStatus, 0)
            );
            for (ExamRecord r : records) {
                r.setStatus(1);
                r.setSubmitTime(task.getEndTime());
                recordMapper.updateById(r);
            }
            throw new BusinessException("考试已结束");
        }

        // 2. 检查是否有进行中的记录
        List<ExamRecord> allRecords = recordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getTaskId, taskId)
                        .eq(ExamRecord::getUserId, currentUserId)
        );

        ExamRecord inProgress = allRecords.stream()
                .filter(r -> r.getStatus() == 0)
                .findFirst()
                .orElse(null);

        if (inProgress != null) {
            // “加入考试”按钮逻辑：如果有进行中的，直接返回
            return inProgress.getId();
        }

        // 3. 检查是否可以开启新纪录 (第一次或上次已提交)
        // 计算已尝试次数 (所有记录，或仅限已提交/已批改？用户要求：若上次已经提交则开始一个新的)
        int attemptCount = allRecords.size();
        
        // 允许重试且次数未达上限 (如果 allowRetry=0，则 maxRetryTimes 默认为 1 比较合理，或者这里做判断)
        Integer maxAllowed = task.getMaxRetryTimes();
        if (maxAllowed == null || maxAllowed <= 0) {
            maxAllowed = 1; // 兜底：如果不允许重做，则只能考1次
        }
        
        if (attemptCount >= maxAllowed) {
            throw new BusinessException("已达到最大考试次数限制");
        }

        // 4. 创建新答题记录
        ExamRecord record = new ExamRecord();
        record.setTaskId(taskId);
        record.setUserId(currentUserId);
        record.setStatus(0); // 进行中
        record.setStartTime(now);
        recordMapper.insert(record);

        log.info("学生开启考试(新纪录), taskId={}, userId={}, recordId={}, attempt={}", 
                taskId, currentUserId, record.getId(), attemptCount + 1);
        return record.getId();
    }

    /**
     * 合并状态计算名 (兼容原有调用)
     */
    private int calculateStatus(LocalDateTime startTime, LocalDateTime endTime) {
        return calculateExamStatus(startTime, endTime);
    }

    /**
     * 计算考试状态
     * @param startTime 开始时间
     * @param endTime 结束时间 (可为null,表示不限制结束时间)
     */
    private int calculateExamStatus(LocalDateTime startTime, LocalDateTime endTime) {
        // 如果开始时间未设置,默认为未开始
        if (startTime == null) {
            return 0; // 未开始
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return 0; // 未开始
        } else if (endTime != null && now.isAfter(endTime)) {
            // 只有设置了结束时间才判断是否已结束
            return 2; // 已结束
        } else {
            return 1; // 进行中
        }
    }
}

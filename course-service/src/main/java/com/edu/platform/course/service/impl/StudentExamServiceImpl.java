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

        // 3. 查询学生的答题记录
        List<Long> taskIds = resultPage.getRecords().stream()
                .map(CourseTask::getId)
                .collect(Collectors.toList());

        Map<Long, ExamRecord> recordMap = Map.of();
        if (!taskIds.isEmpty()) {
            List<ExamRecord> records = recordMapper.selectList(
                    new LambdaQueryWrapper<ExamRecord>()
                            .in(ExamRecord::getTaskId, taskIds)
                            .eq(ExamRecord::getUserId, currentUserId)
            );
            recordMap = records.stream()
                    .collect(Collectors.toMap(ExamRecord::getTaskId, r -> r));
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

            // 计算考试状态
            int examStatus = calculateExamStatus(task.getStartTime(), task.getEndTime());
            response.setExamStatus(examStatus);

            // 查询试卷信息
            List<CourseTaskQuestion> questions = taskQuestionMapper.selectList(
                    new LambdaQueryWrapper<CourseTaskQuestion>()
                            .eq(CourseTaskQuestion::getTaskId, task.getId())
            );
            response.setQuestionCount(questions.size());

            // 学生答题状态
            ExamRecord record = recordMap.get(task.getId());
            if (record != null) {
                response.setStudentStatus(record.getStatus());
                response.setStudentScore(record.getTotalScore());
            } else {
                response.setStudentStatus(0); // 未开始
            }

            responses.add(response);
        }

        return PageResult.of(resultPage.getTotal(), responses);
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
        return paperService.getPaperDetail(taskId);
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
            throw new BusinessException("考试已结束");
        }

        // 2. 检查是否已经开始过
        ExamRecord existingRecord = recordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getTaskId, taskId)
                        .eq(ExamRecord::getUserId, currentUserId)
        );

        if (existingRecord != null) {
            if (existingRecord.getStatus() == 1) {
                throw new BusinessException("已提交答卷,无法重新开始");
            }
            return existingRecord.getId(); // 返回已有记录
        }

        // 3. 创建答题记录
        ExamRecord record = new ExamRecord();
        record.setTaskId(taskId);
        record.setUserId(currentUserId);
        record.setStatus(0); // 进行中
        record.setStartTime(now);
        recordMapper.insert(record);

        log.info("学生开始考试, taskId={}, userId={}, recordId={}", taskId, currentUserId, record.getId());
        return record.getId();
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

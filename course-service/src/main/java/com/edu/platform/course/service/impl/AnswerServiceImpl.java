package com.edu.platform.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.course.dto.request.SaveAnswerRequest;
import com.edu.platform.course.dto.response.AnswerProgressResponse;
import com.edu.platform.course.dto.response.PaperResponse;
import com.edu.platform.course.entity.*;
import com.edu.platform.course.mapper.*;
import com.edu.platform.course.service.AnswerService;
import com.edu.platform.course.service.GradingService;
import com.edu.platform.course.service.PaperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 答题服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final ExamRecordMapper recordMapper;
    private final ExamStudentAnswerMapper answerMapper;
    private final CourseTaskMapper taskMapper;
    private final CourseTaskQuestionMapper taskQuestionMapper;
    private final PaperService paperService;
    private final GradingService gradingService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAnswer(SaveAnswerRequest request) {
        Long currentUserId = UserContext.getUserId();

        // 1. 验证答题记录
        ExamRecord record = recordMapper.selectById(request.getRecordId());
        if (record == null) {
            throw new BusinessException("答题记录不存在");
        }

        if (!record.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权操作此答题记录");
        }

        if (record.getStatus() != 0) {
            throw new BusinessException("答卷已提交,无法修改");
        }

        // 2. 验证试卷题目ID是否有效
        CourseTaskQuestion taskQuestion = taskQuestionMapper.selectOne(
                new LambdaQueryWrapper<CourseTaskQuestion>()
                        .eq(CourseTaskQuestion::getId, request.getTaskQuestionId())
                        .eq(CourseTaskQuestion::getTaskId, record.getTaskId())
        );
        
        if (taskQuestion == null) {
            throw new BusinessException("题目不存在或不属于该试卷");
        }

        // 3. 检查考试是否已结束
        CourseTask task = taskMapper.selectById(record.getTaskId());
        if (task != null && task.getEndTime() != null && LocalDateTime.now().isAfter(task.getEndTime())) {
            throw new BusinessException("考试已结束");
        }

        // 3. 保存或更新答案
        ExamStudentAnswer existingAnswer = answerMapper.selectOne(
                new LambdaQueryWrapper<ExamStudentAnswer>()
                        .eq(ExamStudentAnswer::getRecordId, request.getRecordId())
                        .eq(ExamStudentAnswer::getTaskQuestionId, request.getTaskQuestionId())
        );

        if (existingAnswer != null) {
            // 更新答案
            existingAnswer.setUserAnswer(request.getUserAnswer());
            answerMapper.updateById(existingAnswer);
        } else {
            // 新增答案
            ExamStudentAnswer answer = new ExamStudentAnswer();
            answer.setRecordId(request.getRecordId());
            answer.setTaskQuestionId(request.getTaskQuestionId());
            answer.setUserAnswer(request.getUserAnswer());
            answerMapper.insert(answer);
        }

        log.info("保存答案成功, recordId={}, taskQuestionId={}", request.getRecordId(), request.getTaskQuestionId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitExam(Long recordId) {
        Long currentUserId = UserContext.getUserId();

        // 1. 验证答题记录
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("答题记录不存在");
        }

        if (!record.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权操作此答题记录");
        }

        if (record.getStatus() != 0) {
            throw new BusinessException("答卷已提交");
        }

        // 2. 更新提交状态
        record.setStatus(1); // 已提交
        record.setSubmitTime(LocalDateTime.now());
        recordMapper.updateById(record);

        // 3. 更新任务提交计数
        CourseTask task = taskMapper.selectById(record.getTaskId());
        if (task != null) {
            task.setSubmitCount((task.getSubmitCount() == null ? 0 : task.getSubmitCount()) + 1);
            taskMapper.updateById(task);
        }

        // 4. 触发自动评分
        try {
            gradingService.autoGrade(recordId);
        } catch (Exception e) {
            log.error("自动评分失败, recordId={}", recordId, e);
            // 不影响提交流程
        }

        log.info("提交答卷成功, recordId={}, userId={}", recordId, currentUserId);
    }

    @Override
    public AnswerProgressResponse getAnswerProgress(Long recordId) {
        Long currentUserId = UserContext.getUserId();

        // 1. 验证答题记录
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("答题记录不存在");
        }

        if (!record.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权查看此答题记录");
        }

        // 2. 查询试卷
        PaperResponse paper = paperService.getPaperDetail(record.getTaskId());

        // 3. 查询已答题目
        List<ExamStudentAnswer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<ExamStudentAnswer>()
                        .eq(ExamStudentAnswer::getRecordId, recordId)
        );

        Map<Long, String> answerMap = new HashMap<>();
        for (ExamStudentAnswer answer : answers) {
            answerMap.put(answer.getTaskQuestionId(), answer.getUserAnswer());
        }

        // 4. 构建响应
        AnswerProgressResponse response = new AnswerProgressResponse();
        response.setRecordId(recordId);
        response.setTaskId(record.getTaskId());
        response.setStatus(record.getStatus());
        response.setStartTime(record.getStartTime());
        response.setSubmitTime(record.getSubmitTime());
        response.setTotalScore(record.getTotalScore());
        response.setAnsweredCount(answers.size());
        response.setTotalCount(paper.getQuestionCount());
        response.setAnswers(answerMap);
        response.setPaper(paper);

        return response;
    }
}

package com.edu.platform.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.course.dto.request.GradeAnswerRequest;
import com.edu.platform.course.dto.response.GradingResultResponse;
import com.edu.platform.course.entity.*;
import com.edu.platform.course.mapper.*;
import com.edu.platform.course.service.GradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评分服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradingServiceImpl implements GradingService {

    private final ExamRecordMapper recordMapper;
    private final ExamStudentAnswerMapper answerMapper;
    private final ExamQuestionMapper questionMapper;
    private final ExamQuestionOptionMapper optionMapper;
    private final CourseTaskQuestionMapper taskQuestionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoGrade(Long recordId) {
        // 1. 查询答题记录
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("答题记录不存在");
        }

        // 2. 查询所有答案
        List<ExamStudentAnswer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<ExamStudentAnswer>()
                        .eq(ExamStudentAnswer::getRecordId, recordId)
        );

        if (answers.isEmpty()) {
            log.warn("答题记录为空, recordId={}", recordId);
            return;
        }

        // 3. 查询试卷题目信息
        List<Long> taskQuestionIds = answers.stream()
                .map(ExamStudentAnswer::getTaskQuestionId)
                .collect(Collectors.toList());

        List<CourseTaskQuestion> taskQuestions = taskQuestionMapper.selectBatchIds(taskQuestionIds);
        Map<Long, CourseTaskQuestion> taskQuestionMap = taskQuestions.stream()
                .collect(Collectors.toMap(CourseTaskQuestion::getId, tq -> tq));

        // 4. 查询题库中的题目
        List<Long> questionIds = taskQuestions.stream()
                .map(CourseTaskQuestion::getQuestionId)
                .collect(Collectors.toList());

        List<ExamQuestion> questions = questionMapper.selectBatchIds(questionIds);
        Map<Long, ExamQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(ExamQuestion::getId, q -> q));

        // 5. 自动评分
        BigDecimal totalScore = BigDecimal.ZERO;

        for (ExamStudentAnswer answer : answers) {
            CourseTaskQuestion taskQuestion = taskQuestionMap.get(answer.getTaskQuestionId());
            if (taskQuestion == null) continue;

            ExamQuestion question = questionMap.get(taskQuestion.getQuestionId());
            if (question == null) continue;

            // 只评分客观题(单选、多选、判断)
            if (question.getQuestionType() <= 3) {
                boolean isCorrect = gradeObjectiveQuestion(question, answer.getUserAnswer());
                
                answer.setIsCorrect(isCorrect ? 1 : 0);
                
                if (isCorrect) {
                    BigDecimal questionScore = taskQuestion.getScore();
                    answer.setScore(questionScore);
                    totalScore = totalScore.add(questionScore);
                } else {
                    answer.setScore(BigDecimal.ZERO);
                }
                
                answerMapper.updateById(answer);
            }
        }

        // 6. 更新总分
        record.setTotalScore(totalScore);
        record.setStatus(2); // 已批改(客观题自动批改完成)
        recordMapper.updateById(record);

        log.info("自动评分完成, recordId={}, totalScore={}", recordId, totalScore);
    }

    @Override
    public PageResult<GradingResultResponse> getPendingGrading(Long taskId, Integer pageNum, Integer pageSize) {
        // 1. 查询该任务的所有答题记录
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getTaskId, taskId)
                .in(ExamRecord::getStatus, Arrays.asList(1, 2)); // 已提交或已批改

        Page<ExamRecord> page = new Page<>(pageNum, pageSize);
        Page<ExamRecord> resultPage = recordMapper.selectPage(page, wrapper);

        // 2. 构建响应
        List<GradingResultResponse> responses = new ArrayList<>();
        for (ExamRecord record : resultPage.getRecords()) {
            GradingResultResponse response = getGradingResult(record.getId());
            responses.add(response);
        }

        return PageResult.of(resultPage.getTotal(), responses);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void gradeAnswer(GradeAnswerRequest request) {
        // 查询答案
        ExamStudentAnswer answer = answerMapper.selectById(request.getAnswerId());
        if (answer == null) {
            throw new BusinessException("答案不存在");
        }

        // 更新分数和评语
        answer.setScore(request.getScore());
        answer.setComment(request.getComment());
        answerMapper.updateById(answer);

        // 重新计算总分
        recalculateTotalScore(answer.getRecordId());

        log.info("批改答案成功, answerId={}, score={}", request.getAnswerId(), request.getScore());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishGrade(Long recordId) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("答题记录不存在");
        }

        record.setStatus(2); // 已批改
        record.setGraderId(UserContext.getUserId()); // 记录批改人
        recordMapper.updateById(record);

        log.info("发布成绩成功, recordId={}, graderId={}", recordId, record.getGraderId());
    }

    @Override
    public GradingResultResponse getGradingResult(Long recordId) {
        // 1. 查询答题记录
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("答题记录不存在");
        }

        // 2. 查询答案详情
        List<ExamStudentAnswer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<ExamStudentAnswer>()
                        .eq(ExamStudentAnswer::getRecordId, recordId)
        );

        // 3. 查询试卷题目信息
        List<Long> taskQuestionIds = answers.stream()
                .map(ExamStudentAnswer::getTaskQuestionId)
                .collect(Collectors.toList());

        Map<Long, CourseTaskQuestion> taskQuestionMap = Map.of();
        Map<Long, ExamQuestion> questionMap = Map.of();
        
        if (!taskQuestionIds.isEmpty()) {
            List<CourseTaskQuestion> taskQuestions = taskQuestionMapper.selectBatchIds(taskQuestionIds);
            taskQuestionMap = taskQuestions.stream()
                    .collect(Collectors.toMap(CourseTaskQuestion::getId, tq -> tq));

            List<Long> questionIds = taskQuestions.stream()
                    .map(CourseTaskQuestion::getQuestionId)
                    .collect(Collectors.toList());

            List<ExamQuestion> questions = questionMapper.selectBatchIds(questionIds);
            questionMap = questions.stream()
                    .collect(Collectors.toMap(ExamQuestion::getId, q -> q));
        }

        // 4. 构建响应
        GradingResultResponse response = new GradingResultResponse();
        response.setRecordId(recordId);
        response.setStudentId(record.getUserId());
        response.setTotalScore(record.getTotalScore());

        List<GradingResultResponse.AnswerDetailVO> answerDetails = new ArrayList<>();
        int pendingCount = 0;

        for (ExamStudentAnswer answer : answers) {
            CourseTaskQuestion taskQuestion = taskQuestionMap.get(answer.getTaskQuestionId());
            if (taskQuestion == null) continue;

            ExamQuestion question = questionMap.get(taskQuestion.getQuestionId());
            if (question == null) continue;

            GradingResultResponse.AnswerDetailVO detail = new GradingResultResponse.AnswerDetailVO();
            detail.setAnswerId(answer.getId());
            detail.setTaskQuestionId(taskQuestion.getId());
            detail.setQuestionId(question.getId());
            detail.setQuestionContent(question.getContent());
            detail.setQuestionType(question.getQuestionType());
            detail.setUserAnswer(answer.getUserAnswer());
            detail.setCorrectAnswer(question.getAnswer());
            detail.setScore(answer.getScore());
            detail.setIsCorrect(answer.getIsCorrect() == 1);
            detail.setComment(answer.getComment());

            answerDetails.add(detail);

            // 统计待批改题目(主观题且未评分)
            if (question.getQuestionType() > 3 && answer.getScore() == null) {
                pendingCount++;
            }
        }

        response.setAnswers(answerDetails);
        response.setPendingCount(pendingCount);
        response.setGradingStatus(pendingCount == 0 ? 2 : (pendingCount == answers.size() ? 0 : 1));

        return response;
    }

    /**
     * 评分客观题
     */
    private boolean gradeObjectiveQuestion(ExamQuestion question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }

        // 查询正确答案
        List<ExamQuestionOption> options = optionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestionOption>()
                        .eq(ExamQuestionOption::getQuestionId, question.getId())
                        .eq(ExamQuestionOption::getIsCorrect, 1)
        );

        if (options.isEmpty()) {
            return false;
        }

        // 构建正确答案字符串
        String correctAnswer = options.stream()
                .map(ExamQuestionOption::getOptionLabel)
                .sorted()
                .collect(Collectors.joining(","));

        // 标准化用户答案
        String normalizedUserAnswer = Arrays.stream(userAnswer.split(","))
                .map(String::trim)
                .sorted()
                .collect(Collectors.joining(","));

        return correctAnswer.equalsIgnoreCase(normalizedUserAnswer);
    }

    /**
     * 重新计算总分
     */
    private void recalculateTotalScore(Long recordId) {
        List<ExamStudentAnswer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<ExamStudentAnswer>()
                        .eq(ExamStudentAnswer::getRecordId, recordId)
        );

        BigDecimal totalScore = answers.stream()
                .map(ExamStudentAnswer::getScore)
                .filter(score -> score != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ExamRecord record = recordMapper.selectById(recordId);
        record.setTotalScore(totalScore);
        recordMapper.updateById(record);
    }
}

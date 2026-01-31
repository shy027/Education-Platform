package com.edu.platform.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.course.dto.request.ManualPaperRequest;
import com.edu.platform.course.dto.request.RandomPaperRequest;
import com.edu.platform.course.dto.response.PaperResponse;
import com.edu.platform.course.entity.*;
import com.edu.platform.course.mapper.*;
import com.edu.platform.course.service.PaperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 试卷管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final CourseTaskMapper taskMapper;
    private final CourseTaskQuestionMapper taskQuestionMapper;
    private final ExamQuestionMapper questionMapper;
    private final ExamQuestionOptionMapper optionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assembleManualPaper(ManualPaperRequest request) {
        // 1. 验证任务存在且有权限
        CourseTask task = taskMapper.selectById(request.getTaskId());
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        Long currentUserId = UserContext.getUserId();
        if (!task.getCreatorId().equals(currentUserId)) {
            throw new BusinessException("无权操作此任务");
        }

        // 2. 删除旧的试卷题目
        taskQuestionMapper.delete(new LambdaQueryWrapper<CourseTaskQuestion>()
                .eq(CourseTaskQuestion::getTaskId, request.getTaskId()));

        // 3. 添加新题目
        for (ManualPaperRequest.QuestionItem item : request.getQuestions()) {
            // 验证题目存在
            ExamQuestion question = questionMapper.selectById(item.getQuestionId());
            if (question == null || question.getIsDeleted() == 1) {
                throw new BusinessException("题目不存在: " + item.getQuestionId());
            }

            CourseTaskQuestion taskQuestion = new CourseTaskQuestion();
            taskQuestion.setTaskId(request.getTaskId());
            taskQuestion.setQuestionId(item.getQuestionId());
            taskQuestion.setScore(item.getScore());
            taskQuestion.setSortOrder(item.getSortOrder());
            taskQuestionMapper.insert(taskQuestion);
        }

        // 4. 更新试卷状态为已组卷
        task.setPaperStatus(1);
        taskMapper.updateById(task);

        log.info("手动组卷成功, taskId={}, questionCount={}", request.getTaskId(), request.getQuestions().size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assembleRandomPaper(RandomPaperRequest request) {
        // 1. 验证任务存在且有权限
        CourseTask task = taskMapper.selectById(request.getTaskId());
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        Long currentUserId = UserContext.getUserId();
        if (!task.getCreatorId().equals(currentUserId)) {
            throw new BusinessException("无权操作此任务");
        }

        // 2. 删除旧的试卷题目
        taskQuestionMapper.delete(new LambdaQueryWrapper<CourseTaskQuestion>()
                .eq(CourseTaskQuestion::getTaskId, request.getTaskId()));

        // 3. 根据配置随机抽取题目
        List<ExamQuestion> selectedQuestions = new ArrayList<>();
        int sortOrder = 1;

        for (Map.Entry<Integer, Integer> entry : request.getTypeCount().entrySet()) {
            Integer questionType = entry.getKey();
            Integer count = entry.getValue();

            // 查询该题型的题目
            LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamQuestion::getCourseId, request.getCourseId())
                    .eq(ExamQuestion::getType, questionType)  // 使用实际字段 type
                    .eq(ExamQuestion::getIsDeleted, 0)
                    .eq(ExamQuestion::getStatus, 1);

            // 如果指定了难度分布
            if (request.getDifficultyCount() != null && !request.getDifficultyCount().isEmpty()) {
                List<Integer> difficulties = new ArrayList<>(request.getDifficultyCount().keySet());
                wrapper.in(ExamQuestion::getDifficulty, difficulties);
            }

            wrapper.last("ORDER BY RAND() LIMIT " + count);
            List<ExamQuestion> questions = questionMapper.selectList(wrapper);

            if (questions.size() < count) {
                throw new BusinessException("题型 " + questionType + " 的题目数量不足");
            }

            selectedQuestions.addAll(questions);
        }

        // 4. 保存题目到试卷
        for (ExamQuestion question : selectedQuestions) {
            CourseTaskQuestion taskQuestion = new CourseTaskQuestion();
            taskQuestion.setTaskId(request.getTaskId());
            taskQuestion.setQuestionId(question.getId());
            
            // 根据题型设置分值
            Double scorePerType = request.getScorePerType().get(question.getQuestionType());
            taskQuestion.setScore(BigDecimal.valueOf(scorePerType != null ? scorePerType : 1.0));
            taskQuestion.setSortOrder(sortOrder++);
            
            taskQuestionMapper.insert(taskQuestion);
        }

        // 4. 更新试卷状态为已组卷
        task.setPaperStatus(1);
        taskMapper.updateById(task);

        log.info("随机组卷成功, taskId={}, questionCount={}", request.getTaskId(), selectedQuestions.size());
    }

    @Override
    public PaperResponse getPaperDetail(Long taskId) {
        // 1. 查询任务
        CourseTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        // 2. 查询试卷题目
        List<CourseTaskQuestion> taskQuestions = taskQuestionMapper.selectList(
                new LambdaQueryWrapper<CourseTaskQuestion>()
                        .eq(CourseTaskQuestion::getTaskId, taskId)
                        .orderByAsc(CourseTaskQuestion::getSortOrder)
        );

        if (taskQuestions.isEmpty()) {
            throw new BusinessException("试卷未组卷");
        }

        // 3. 查询题目详情
        List<Long> questionIds = taskQuestions.stream()
                .map(CourseTaskQuestion::getQuestionId)
                .collect(Collectors.toList());

        List<ExamQuestion> questions = questionMapper.selectBatchIds(questionIds);
        Map<Long, ExamQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(ExamQuestion::getId, q -> q));

        // 4. 构建响应
        PaperResponse response = new PaperResponse();
        response.setTaskId(taskId);
        response.setTitle(task.getTaskTitle());
        response.setCreatedTime(task.getCreatedTime());

        List<PaperResponse.PaperQuestionVO> questionVOs = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;

        for (CourseTaskQuestion tq : taskQuestions) {
            ExamQuestion question = questionMap.get(tq.getQuestionId());
            if (question == null) continue;

            PaperResponse.PaperQuestionVO vo = new PaperResponse.PaperQuestionVO();
            vo.setQuestionId(question.getId());
            vo.setContent(question.getContent());
            vo.setQuestionType(question.getQuestionType());
            vo.setTypeName(getTypeName(question.getQuestionType()));
            vo.setScore(tq.getScore());
            vo.setSortOrder(tq.getSortOrder());

            // 查询选项(选择题和判断题)
            if (question.getQuestionType() != null && question.getQuestionType() <= 3) {
                List<ExamQuestionOption> options = optionMapper.selectList(
                        new LambdaQueryWrapper<ExamQuestionOption>()
                                .eq(ExamQuestionOption::getQuestionId, question.getId())
                                .orderByAsc(ExamQuestionOption::getSortOrder)
                );

                List<PaperResponse.OptionVO> optionVOs = options.stream()
                        .map(this::buildOptionVO)
                        .collect(Collectors.toList());
                vo.setOptions(optionVOs);
            }

            questionVOs.add(vo);
            totalScore = totalScore.add(tq.getScore());
        }

        response.setQuestions(questionVOs);
        response.setQuestionCount(questionVOs.size());
        response.setTotalScore(totalScore);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePaper(Long taskId) {
        // 验证权限
        CourseTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        Long currentUserId = UserContext.getUserId();
        if (!task.getCreatorId().equals(currentUserId)) {
            throw new BusinessException("无权操作此任务");
        }

        // 删除试卷题目
        taskQuestionMapper.delete(new LambdaQueryWrapper<CourseTaskQuestion>()
                .eq(CourseTaskQuestion::getTaskId, taskId));

        log.info("删除试卷成功, taskId={}", taskId);
    }

    /**
     * 构建选项VO
     */
    private PaperResponse.OptionVO buildOptionVO(ExamQuestionOption option) {
        PaperResponse.OptionVO vo = new PaperResponse.OptionVO();
        vo.setId(option.getId());
        vo.setOptionLabel(option.getOptionLabel());
        vo.setContent(option.getContent());
        vo.setIsCorrect(option.getIsCorrect() == 1);
        vo.setSortOrder(option.getSortOrder());
        return vo;
    }

    /**
     * 获取题型名称
     */
    private String getTypeName(Integer type) {
        switch (type) {
            case 1: return "单选题";
            case 2: return "多选题";
            case 3: return "判断题";
            case 4: return "填空题";
            case 5: return "简答题";
            case 6: return "编程题";
            default: return "未知";
        }
    }
}

package com.edu.platform.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.course.dto.request.ManualPaperRequest;
import com.edu.platform.course.dto.request.RandomPaperRequest;
import com.edu.platform.course.dto.response.PaperResponse;
import com.edu.platform.course.dto.response.QuestionResponse;
import com.edu.platform.course.entity.CourseTask;
import com.edu.platform.course.entity.CourseTaskQuestion;
import com.edu.platform.course.entity.ExamQuestion;
import com.edu.platform.course.entity.ExamQuestionOption;
import com.edu.platform.course.mapper.CourseTaskMapper;
import com.edu.platform.course.mapper.CourseTaskQuestionMapper;
import com.edu.platform.course.mapper.ExamQuestionMapper;
import com.edu.platform.course.mapper.ExamQuestionOptionMapper;
import com.edu.platform.course.service.PaperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 试卷服务实现类
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
    public Long assembleManualPaper(ManualPaperRequest request) {
        Long currentUserId = UserContext.getUserId();
        CourseTask task;
        
        // 1. 获取或创建任务
        if (request.getTaskId() == null) {
            // 创建新任务
            task = new CourseTask();
            task.setCourseId(request.getCourseId());
            task.setCreatorId(currentUserId);
            task.setTaskType(3); // 默认考试类型
            task.setStatus(0);    // 默认草稿状态
            task.setSubmitCount(0);
            task.setPaperStatus(0);
        } else {
            // 获取已有任务
            task = taskMapper.selectById(request.getTaskId());
            if (task == null) {
                throw new BusinessException("任务不存在");
            }
            if (!task.getCreatorId().equals(currentUserId)) {
                throw new BusinessException("无权操作此任务");
            }
        }

        // 更新基础信息 (如果提供了)
        if (StrUtil.isNotBlank(request.getTaskTitle())) {
            task.setTaskTitle(request.getTaskTitle());
        }
        if (request.getTaskDescription() != null) {
            task.setTaskDescription(request.getTaskDescription());
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

        // 2. 保存任务基本信息
        if (task.getId() == null) {
            taskMapper.insert(task);
        } else {
            taskMapper.updateById(task);
        }
        
        Long taskId = task.getId();

        // 3. 删除旧的试卷题目
        taskQuestionMapper.delete(new LambdaQueryWrapper<CourseTaskQuestion>()
                .eq(CourseTaskQuestion::getTaskId, taskId));

        // 4. 添加新题目
        for (ManualPaperRequest.QuestionItem item : request.getQuestions()) {
            ExamQuestion question = questionMapper.selectById(item.getQuestionId());
            if (question == null || question.getIsDeleted() == 1) {
                throw new BusinessException("题目不存在: " + item.getQuestionId());
            }

            CourseTaskQuestion taskQuestion = new CourseTaskQuestion();
            taskQuestion.setTaskId(taskId);
            taskQuestion.setQuestionId(item.getQuestionId());
            taskQuestion.setScore(item.getScore());
            taskQuestion.setSortOrder(item.getSortOrder());
            taskQuestionMapper.insert(taskQuestion);
        }

        // 5. 更新总分与组卷状态
        BigDecimal totalScore = request.getQuestions().stream()
                .map(ManualPaperRequest.QuestionItem::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        task.setPaperStatus(1);
        task.setTotalScore(totalScore);
        taskMapper.updateById(task);

        log.info("组卷设置成功, taskId={}, questionCount={}", taskId, request.getQuestions().size());
        return taskId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long assembleRandomPaper(RandomPaperRequest request) {
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
                    .eq(ExamQuestion::getType, questionType)
                    .eq(ExamQuestion::getIsDeleted, 0)
                    .eq(ExamQuestion::getStatus, 1);

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
            
            Double scorePerType = request.getScorePerType().get(question.getQuestionType());
            taskQuestion.setScore(BigDecimal.valueOf(scorePerType != null ? scorePerType : 1.0));
            taskQuestion.setSortOrder(sortOrder++);
            
            taskQuestionMapper.insert(taskQuestion);
        }

        // 5. 更新试卷状态与总分
        BigDecimal totalScore = selectedQuestions.stream()
                .map(q -> {
                    Double s = request.getScorePerType().get(q.getQuestionType());
                    return BigDecimal.valueOf(s != null ? s : 1.0);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        task.setPaperStatus(1);
        task.setTotalScore(totalScore);
        
        if (request.getAllowRetry() != null) {
            task.setAllowRetry(request.getAllowRetry());
        }
        if (request.getMaxRetryTimes() != null) {
            task.setMaxRetryTimes(request.getMaxRetryTimes());
        }
        if (request.getShowAnswer() != null) {
            task.setShowAnswer(request.getShowAnswer());
        }
        
        taskMapper.updateById(task);

        log.info("随机组卷成功, taskId={}, questionCount={}", request.getTaskId(), selectedQuestions.size());
        return task.getId();
    }

    @Override
    public PaperResponse getPaperDetail(Long taskId, boolean includeAnswers) {
        CourseTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        PaperResponse response = new PaperResponse();
        BeanUtil.copyProperties(task, response);
        
        // 补充时间信息
        response.setStartTime(task.getStartTime());
        response.setEndTime(task.getEndTime());
        response.setDurationMinutes(task.getDurationMinutes());

        List<CourseTaskQuestion> taskQuestions = taskQuestionMapper.selectList(
                new LambdaQueryWrapper<CourseTaskQuestion>()
                        .eq(CourseTaskQuestion::getTaskId, taskId)
                        .orderByAsc(CourseTaskQuestion::getSortOrder));

        List<PaperResponse.PaperQuestionVO> questions = taskQuestions.stream().map(tq -> {
            ExamQuestion question = questionMapper.selectById(tq.getQuestionId());
            PaperResponse.PaperQuestionVO qr = new PaperResponse.PaperQuestionVO();
            BeanUtil.copyProperties(question, qr);
            qr.setQuestionId(question.getId()); // 明确设置 ID
            qr.setTaskQuestionId(tq.getId());   // 关联表主键，用于提交答案
            qr.setScore(tq.getScore());
            qr.setSortOrder(tq.getSortOrder());
            
            // 明确设置题型，防止 BeanUtil 未调用 getter 导致丢失
            qr.setQuestionType(question.getQuestionType());
            
            // 设置题型名称
            qr.setTypeName(getQuestionTypeName(question.getQuestionType()));
            
            // 如果不包含答案，清空答案字段
            if (!includeAnswers) {
                qr.setCorrectAnswer(null);
                qr.setReferenceAnswer(null);
                qr.setAnalysis(null);
            }

            if (question.getQuestionType() != null && (question.getQuestionType() == 1 || question.getQuestionType() == 2)) {
                List<ExamQuestionOption> options = optionMapper.selectList(
                        new LambdaQueryWrapper<ExamQuestionOption>()
                                .eq(ExamQuestionOption::getQuestionId, question.getId())
                                .orderByAsc(ExamQuestionOption::getSortOrder));
                
                qr.setOptions(options.stream().map(opt -> {
                    PaperResponse.OptionVO vo = new PaperResponse.OptionVO();
                    vo.setId(opt.getId());
                    vo.setOptionLabel(opt.getOptionLabel());
                    vo.setContent(opt.getContent());
                    vo.setSortOrder(opt.getSortOrder());
                    // Integer -> Boolean 转换
                    if (includeAnswers && opt.getIsCorrect() != null) {
                        vo.setIsCorrect(opt.getIsCorrect() == 1);
                    }
                    return vo;
                }).collect(Collectors.toList()));
            }
            return qr;
        }).collect(Collectors.toList());

        response.setQuestions(questions);
        return response;
    }

    private String getQuestionTypeName(Integer type) {
        if (type == null) return "未知";
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePaper(Long taskId) {
        taskQuestionMapper.delete(new LambdaQueryWrapper<CourseTaskQuestion>()
                .eq(CourseTaskQuestion::getTaskId, taskId));
        
        CourseTask task = taskMapper.selectById(taskId);
        if (task != null) {
            task.setPaperStatus(0);
            taskMapper.updateById(task);
        }
    }
}

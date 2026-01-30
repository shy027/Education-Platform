package com.edu.platform.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.course.dto.request.QuestionCreateRequest;
import com.edu.platform.course.dto.request.QuestionQueryRequest;
import com.edu.platform.course.dto.request.QuestionUpdateRequest;
import com.edu.platform.course.dto.response.QuestionResponse;
import com.edu.platform.course.entity.*;
import com.edu.platform.course.mapper.*;
import com.edu.platform.course.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final ExamQuestionMapper questionMapper;
    private final ExamQuestionOptionMapper optionMapper;
    private final ExamDimensionMapper dimensionMapper;
    private final ExamQuestionDimensionMapper questionDimensionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestion(QuestionCreateRequest request) {
        Long currentUserId = UserContext.getUserId();

        // 1. 创建题目
        ExamQuestion question = new ExamQuestion();
        question.setCourseId(request.getCourseId());
        question.setContent(request.getContent());
        question.setQuestionType(request.getQuestionType());
        question.setScore(request.getScore());
        question.setDifficulty(request.getDifficulty());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setReferenceAnswer(request.getReferenceAnswer());
        question.setAnalysis(request.getAnalysis());
        question.setCreatorId(currentUserId);

        questionMapper.insert(question);
        Long questionId = question.getId();

        // 2. 创建选项 (选择题和判断题)
        if (request.getQuestionType() != null && request.getQuestionType() <= 3 && !CollectionUtils.isEmpty(request.getOptions())) {
            for (QuestionCreateRequest.QuestionOptionDTO optionDTO : request.getOptions()) {
                ExamQuestionOption option = new ExamQuestionOption();
                option.setQuestionId(questionId);
                option.setOptionLabel(optionDTO.getOptionLabel());
                option.setContent(optionDTO.getContent());
                option.setIsCorrect(optionDTO.getIsCorrect() ? 1 : 0);
                option.setSortOrder(optionDTO.getSortOrder());
                optionMapper.insert(option);
            }
        }

        // 3. 关联维度
        if (!CollectionUtils.isEmpty(request.getDimensionIds())) {
            for (Long dimensionId : request.getDimensionIds()) {
                ExamQuestionDimension qd = new ExamQuestionDimension();
                qd.setQuestionId(questionId);
                qd.setDimensionId(dimensionId);
                questionDimensionMapper.insert(qd);
            }
        }

        log.info("创建题目成功, questionId={}, creatorId={}", questionId, currentUserId);
        return questionId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestion(Long questionId, QuestionUpdateRequest request) {
        // 1. 验证题目存在
        ExamQuestion question = questionMapper.selectById(questionId);
        if (question == null || question.getIsDeleted() == 1) {
            throw new BusinessException("题目不存在");
        }

        // 2. 权限校验：仅创建者或管理员可修改
        Long currentUserId = UserContext.getUserId();
        // TODO: 添加管理员权限判断
        if (!question.getCreatorId().equals(currentUserId)) {
            throw new BusinessException("无权修改此题目");
        }

        // 3. 更新题目基本信息
        if (StringUtils.hasText(request.getContent())) {
            question.setContent(request.getContent());
        }
        if (request.getScore() != null) {
            question.setScore(request.getScore());
        }
        if (StringUtils.hasText(request.getCorrectAnswer())) {
            question.setCorrectAnswer(request.getCorrectAnswer());
        }
        if (StringUtils.hasText(request.getReferenceAnswer())) {
            question.setReferenceAnswer(request.getReferenceAnswer());
        }
        if (StringUtils.hasText(request.getAnalysis())) {
            question.setAnalysis(request.getAnalysis());
        }
        if (request.getDifficulty() != null) {
            question.setDifficulty(request.getDifficulty());
        }
        questionMapper.updateById(question);

        // 4. 更新选项 (先删除后新增)
        if (!CollectionUtils.isEmpty(request.getOptions())) {
            optionMapper.delete(new LambdaQueryWrapper<ExamQuestionOption>()
                    .eq(ExamQuestionOption::getQuestionId, questionId));

            for (QuestionCreateRequest.QuestionOptionDTO optionDTO : request.getOptions()) {
                ExamQuestionOption option = new ExamQuestionOption();
                option.setQuestionId(questionId);
                option.setOptionLabel(optionDTO.getOptionLabel());
                option.setContent(optionDTO.getContent());
                option.setIsCorrect(optionDTO.getIsCorrect() ? 1 : 0);
                option.setSortOrder(optionDTO.getSortOrder());
                optionMapper.insert(option);
            }
        }

        // 5. 更新维度关联
        if (!CollectionUtils.isEmpty(request.getDimensionIds())) {
            questionDimensionMapper.delete(new LambdaQueryWrapper<ExamQuestionDimension>()
                    .eq(ExamQuestionDimension::getQuestionId, questionId));

            for (Long dimensionId : request.getDimensionIds()) {
                ExamQuestionDimension qd = new ExamQuestionDimension();
                qd.setQuestionId(questionId);
                qd.setDimensionId(dimensionId);
                questionDimensionMapper.insert(qd);
            }
        }

        log.info("更新题目成功, questionId={}", questionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long questionId) {
        // 1. 验证题目存在
        ExamQuestion question = questionMapper.selectById(questionId);
        if (question == null || question.getIsDeleted() == 1) {
            throw new BusinessException("题目不存在");
        }

        // 2. 权限校验
        Long currentUserId = UserContext.getUserId();
        if (!question.getCreatorId().equals(currentUserId)) {
            throw new BusinessException("无权删除此题目");
        }

        // 3. 逻辑删除 (使用MyBatis-Plus的deleteById会自动触发@TableLogic)
        questionMapper.deleteById(questionId);

        log.info("删除题目成功, questionId={}", questionId);
    }

    @Override
    public QuestionResponse getQuestionDetail(Long questionId) {
        // 1. 查询题目
        ExamQuestion question = questionMapper.selectById(questionId);
        if (question == null || question.getIsDeleted() == 1) {
            throw new BusinessException("题目不存在");
        }

        // 2. 构建响应
        QuestionResponse response = buildQuestionResponse(question);

        // 3. 查询选项
        if (question.getQuestionType() != null && question.getQuestionType() <= 3) {
            List<ExamQuestionOption> options = optionMapper.selectList(
                    new LambdaQueryWrapper<ExamQuestionOption>()
                            .eq(ExamQuestionOption::getQuestionId, questionId)
                            .orderByAsc(ExamQuestionOption::getSortOrder)
            );

            List<QuestionResponse.QuestionOptionVO> optionVOs = options.stream()
                    .map(this::buildOptionVO)
                    .collect(Collectors.toList());
            response.setOptions(optionVOs);
        }

        // 4. 查询维度关联
        List<ExamQuestionDimension> qds = questionDimensionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestionDimension>()
                        .eq(ExamQuestionDimension::getQuestionId, questionId)
        );

        if (!CollectionUtils.isEmpty(qds)) {
            Set<Long> dimensionIds = qds.stream()
                    .map(ExamQuestionDimension::getDimensionId)
                    .collect(Collectors.toSet());

            List<ExamDimension> dimensions = dimensionMapper.selectBatchIds(dimensionIds);
            Map<Long, String> dimensionMap = dimensions.stream()
                    .collect(Collectors.toMap(ExamDimension::getId, ExamDimension::getName));

            Map<String, BigDecimal> dimensionWeights = qds.stream()
                    .collect(Collectors.toMap(
                            qd -> dimensionMap.get(qd.getDimensionId()),
                            ExamQuestionDimension::getWeight
                    ));
            response.setDimensions(dimensionWeights);
        }

        return response;
    }

    @Override
    public PageResult<QuestionResponse> listQuestions(QuestionQueryRequest request) {
        // 1. 构建查询条件
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestion::getIsDeleted, 0);

        if (request.getCourseId() != null) {
            wrapper.eq(ExamQuestion::getCourseId, request.getCourseId());
        }
        if (request.getChapterId() != null) {
            wrapper.eq(ExamQuestion::getChapterId, request.getChapterId());
        }
        if (request.getQuestionType() != null) {
            wrapper.eq(ExamQuestion::getQuestionType, request.getQuestionType());
        }
        if (request.getDifficulty() != null) {
            wrapper.eq(ExamQuestion::getDifficulty, request.getDifficulty());
        }
        if (request.getCreatorId() != null) {
            wrapper.eq(ExamQuestion::getCreatorId, request.getCreatorId());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(ExamQuestion::getContent, request.getKeyword());
        }

        wrapper.orderByDesc(ExamQuestion::getCreatedTime);

        // 2. 分页查询
        Page<ExamQuestion> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<ExamQuestion> resultPage = questionMapper.selectPage(page, wrapper);

        // 3. 构建响应
        List<QuestionResponse> responses = resultPage.getRecords().stream()
                .map(this::buildQuestionResponse)
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), responses);
    }

    /**
     * 构建题目响应对象
     */
    private QuestionResponse buildQuestionResponse(ExamQuestion question) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setCourseId(question.getCourseId());
        response.setChapterId(question.getChapterId());
        response.setContent(question.getContent());
        response.setQuestionType(question.getQuestionType());
        response.setTypeName(getTypeName(question.getQuestionType()));
        response.setScore(question.getScore());
        response.setCorrectAnswer(question.getCorrectAnswer());
        response.setReferenceAnswer(question.getReferenceAnswer());
        response.setAnalysis(question.getAnalysis());
        response.setDifficulty(question.getDifficulty());
        response.setCreatorId(question.getCreatorId());
        response.setStatus(question.getStatus());
        response.setCreatedTime(question.getCreatedTime());
        return response;
    }

    /**
     * 构建选项VO
     */
    private QuestionResponse.QuestionOptionVO buildOptionVO(ExamQuestionOption option) {
        QuestionResponse.QuestionOptionVO vo = new QuestionResponse.QuestionOptionVO();
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
            case 1:
                return "单选题";
            case 2:
                return "多选题";
            case 3:
                return "判断题";
            case 4:
                return "简答题";
            default:
                return "未知";
        }
    }
}

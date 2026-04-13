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
    private final SubjectCategoryMapper categoryMapper;
    private final CourseMapper courseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestion(QuestionCreateRequest request) {
        Long currentUserId = UserContext.getUserId();

        // 1. 创建题目
        ExamQuestion question = new ExamQuestion();
        question.setCourseId(request.getCourseId());
        
        // 学科分类映射逻辑
        Long catId = request.getCategoryId();
        if (catId == null && StringUtils.hasText(request.getCategoryName())) {
            // 通过名称查询分类 ID
            SubjectCategory category = categoryMapper.selectOne(new LambdaQueryWrapper<SubjectCategory>()
                    .eq(SubjectCategory::getName, request.getCategoryName().trim())
                    .eq(SubjectCategory::getIsEnabled, 1)
                    .last("LIMIT 1"));
            if (category != null) {
                catId = category.getId();
            }
        }
        question.setCategoryId(catId);
        question.setContent(request.getContent());
        question.setQuestionType(request.getQuestionType());
        question.setScore(request.getScore());
        question.setDifficulty(request.getDifficulty());
        if (request.getQuestionType() != null) {
            if (request.getQuestionType() == 4) {
                question.setAnswer(request.getCorrectAnswer());
            } else if (request.getQuestionType() == 5 || request.getQuestionType() == 6) {
                question.setAnswer(request.getReferenceAnswer());
            } else if (request.getQuestionType() <= 3 && !CollectionUtils.isEmpty(request.getOptions())) {
                // 为选择题和判断题自动计算正确答案标签，存入 answer 字段
                String correctLabels = request.getOptions().stream()
                        .filter(opt -> opt.getIsCorrect() != null && opt.getIsCorrect())
                        .map(QuestionCreateRequest.QuestionOptionDTO::getOptionLabel)
                        .sorted()
                        .collect(Collectors.joining(","));
                question.setAnswer(correctLabels);
            }
        }
        question.setAnalysis(request.getAnalysis());
        question.setDimensions(request.getDimensions());
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

        // 3. (旧维度关联处理已移除，采用直接存入 dimensions 字段)

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
        if (request.getCategoryId() != null) {
            question.setCategoryId(request.getCategoryId());
        }
        if (StringUtils.hasText(request.getDimensions())) {
            question.setDimensions(request.getDimensions());
        }
        if (StringUtils.hasText(request.getContent())) {
            question.setContent(request.getContent());
        }
        if (request.getScore() != null) {
            question.setScore(request.getScore());
        }
        if (question.getQuestionType() != null) {
            if (question.getQuestionType() == 4 && request.getCorrectAnswer() != null) {
                question.setAnswer(request.getCorrectAnswer());
            } else if ((question.getQuestionType() == 5 || question.getQuestionType() == 6) && request.getReferenceAnswer() != null) {
                question.setAnswer(request.getReferenceAnswer());
            }
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

            // 更新题目表的正确答案缓存
            if (question.getQuestionType() != null && question.getQuestionType() <= 3) {
                String correctLabels = request.getOptions().stream()
                        .filter(opt -> opt.getIsCorrect() != null && opt.getIsCorrect())
                        .map(QuestionCreateRequest.QuestionOptionDTO::getOptionLabel)
                        .sorted()
                        .collect(Collectors.joining(","));
                question.setAnswer(correctLabels);
                questionMapper.updateById(question);
            }
        }

        // 5. (旧维度关联处理已移除，更新已在第3步完成)

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

        // 4. (旧版分离维度的处理已被移除)

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
            wrapper.eq(ExamQuestion::getType, request.getQuestionType());  // 使用实际字段 type
        }
        if (request.getDifficulty() != null) {
            wrapper.eq(ExamQuestion::getDifficulty, request.getDifficulty());
        }
        if (request.getCreatorId() != null) {
            wrapper.eq(ExamQuestion::getCreatorId, request.getCreatorId());
        }
        if (request.getBankType() != null) {
            if (request.getBankType() == 1) {
                // 公共题库 (course_id = 0 或 null)
                wrapper.and(w -> w.eq(ExamQuestion::getCourseId, 0).or().isNull(ExamQuestion::getCourseId));
            } else if (request.getBankType() == 2) {
                // 非公共题库 (course_id > 0)
                wrapper.gt(ExamQuestion::getCourseId, 0);
            }
        }

        if (StringUtils.hasText(request.getCategoryId())) {
            String catId = request.getCategoryId();
            
            if ("unclassified".equals(catId)) {
                // “未分类”逻辑：无学科类型id且为公共课程，或所属课程无学科
                wrapper.and(w -> w.and(w1 -> w1.isNull(ExamQuestion::getCategoryId)
                                .and(w2 -> w2.eq(ExamQuestion::getCourseId, 0).or().isNull(ExamQuestion::getCourseId)))
                        .or().inSql(ExamQuestion::getCourseId, "SELECT id FROM course_info WHERE subject_area IS NULL OR subject_area = ''"));
            } else {
                // 获取分类名称用于向 course_info.subject_area 降级匹配
                String catName = null;
                try {
                    SubjectCategory cat = categoryMapper.selectById(catId);
                    if (cat != null) catName = cat.getName();
                } catch (Exception ignored) {}

                final String scName = catName;
                
                // 匹配逻辑：题目直接关联了该分类，或者题目属于与其学科匹配的课程
                wrapper.and(w -> {
                    w.eq(ExamQuestion::getCategoryId, catId);
                    if (StringUtils.hasText(scName)) {
                        // 使用子查询匹配课程所属学科
                        w.or().inSql(ExamQuestion::getCourseId, String.format("SELECT id FROM course_info WHERE subject_area LIKE '%%%s%%'", scName));
                    }
                });
            }
        }

        if (StringUtils.hasText(request.getDimensions())) {
            // 素养维度搜索：必须全部包含所选维度
            String[] dimIds = request.getDimensions().split(",");
            for (String id : dimIds) {
                String trimmedId = id.trim();
                if (StringUtils.hasText(trimmedId)) {
                    wrapper.like(ExamQuestion::getDimensions, trimmedId);
                }
            }
        }
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(ExamQuestion::getContent, request.getKeyword());
        }

        wrapper.orderByDesc(ExamQuestion::getCreatedTime);

        // 2. 分页查询
        Page<ExamQuestion> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<ExamQuestion> resultPage = questionMapper.selectPage(page, wrapper);

        // 3. 构建响应
        // 3. 补充关联信息名称
        List<ExamQuestion> questions = resultPage.getRecords();
        if (questions.isEmpty()) {
            return PageResult.of(resultPage.getTotal(), new ArrayList<>());
        }

        // 批量查询课程名称
        Set<Long> courseIds = questions.stream()
                .map(ExamQuestion::getCourseId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, String> courseNameMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            List<Course> courses = courseMapper.selectBatchIds(courseIds);
            courseNameMap = courses.stream().collect(Collectors.toMap(Course::getId, Course::getCourseName));
        }

        // 批量查询分类名称
        Set<Long> categoryIds = questions.stream()
                .map(ExamQuestion::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<SubjectCategory> categories = categoryMapper.selectBatchIds(categoryIds);
            categoryNameMap = categories.stream().collect(Collectors.toMap(SubjectCategory::getId, SubjectCategory::getName));
        }

        // 批量查询维度名称 (匹配 AdminConfigController 默认规则)
        Map<String, String> dimNameMap = new HashMap<>();
        dimNameMap.put("1", "知识技能素养");
        dimNameMap.put("2", "职业品格素养");
        dimNameMap.put("3", "创新实践素养");
        dimNameMap.put("4", "社会责任素养");
        dimNameMap.put("5", "发展适应素养");

        Map<Long, String> finalCourseNameMap = courseNameMap;
        Map<Long, String> finalCategoryNameMap = categoryNameMap;
        List<QuestionResponse> responses = questions.stream()
                .map(q -> {
                    QuestionResponse resp = buildQuestionResponse(q);
                    resp.setCourseName(finalCourseNameMap.getOrDefault(q.getCourseId(), q.getCourseId() != null && q.getCourseId() > 0 ? "未知课程" : "公共题库"));
                    
                    // 使用分类名称作为展示
                    resp.setCategoryName(finalCategoryNameMap.get(q.getCategoryId()));
                    
                    if (StringUtils.hasText(q.getDimensions())) {
                        String names = Arrays.stream(q.getDimensions().split(","))
                                .map(dimId -> dimNameMap.getOrDefault(dimId.trim(), "维度" + dimId.trim()))
                                .collect(Collectors.joining(", "));
                        resp.setDimensionNames(names);
                    }
                    return resp;
                })
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
        response.setCategoryId(question.getCategoryId());
        response.setChapterId(question.getChapterId());
        response.setContent(question.getContent());
        response.setQuestionType(question.getQuestionType());
        response.setTypeName(getTypeName(question.getQuestionType()));
        response.setScore(question.getScore());
        response.setCorrectAnswer(question.getCorrectAnswer());
        response.setReferenceAnswer(question.getReferenceAnswer());
        response.setAnalysis(question.getAnalysis());
        response.setDimensions(question.getDimensions());
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
}

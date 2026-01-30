package com.edu.platform.course.service;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.course.dto.request.QuestionCreateRequest;
import com.edu.platform.course.dto.request.QuestionQueryRequest;
import com.edu.platform.course.dto.request.QuestionUpdateRequest;
import com.edu.platform.course.dto.response.QuestionResponse;

/**
 * 题目管理服务
 */
public interface QuestionService {

    /**
     * 创建题目
     *
     * @param request 创建请求
     * @return 题目ID
     */
    Long createQuestion(QuestionCreateRequest request);

    /**
     * 更新题目
     *
     * @param questionId 题目ID
     * @param request    更新请求
     */
    void updateQuestion(Long questionId, QuestionUpdateRequest request);

    /**
     * 删除题目
     *
     * @param questionId 题目ID
     */
    void deleteQuestion(Long questionId);

    /**
     * 获取题目详情
     *
     * @param questionId 题目ID
     * @return 题目详情
     */
    QuestionResponse getQuestionDetail(Long questionId);

    /**
     * 查询题目列表
     *
     * @param request 查询请求
     * @return 题目列表
     */
    PageResult<QuestionResponse> listQuestions(QuestionQueryRequest request);
}

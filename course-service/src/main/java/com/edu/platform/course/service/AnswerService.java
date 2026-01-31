package com.edu.platform.course.service;

import com.edu.platform.course.dto.request.GradeAnswerRequest;
import com.edu.platform.course.dto.request.SaveAnswerRequest;
import com.edu.platform.course.dto.response.AnswerProgressResponse;

/**
 * 答题服务
 */
public interface AnswerService {

    /**
     * 保存答案(实时保存)
     */
    void saveAnswer(SaveAnswerRequest request);

    /**
     * 提交答卷(最终提交)
     */
    void submitExam(Long recordId);

    /**
     * 获取答题进度
     */
    AnswerProgressResponse getAnswerProgress(Long recordId);
}

package com.edu.platform.course.service;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.course.dto.request.GradeAnswerRequest;
import com.edu.platform.course.dto.response.GradingResultResponse;

/**
 * 评分服务
 */
public interface GradingService {

    /**
     * 自动评分(客观题)
     * @param recordId 答题记录ID
     */
    void autoGrade(Long recordId);

    /**
     * 获取待批改列表
     */
    PageResult<GradingResultResponse> getPendingGrading(Long taskId, Integer pageNum, Integer pageSize);

    /**
     * 批改单题
     */
    void gradeAnswer(GradeAnswerRequest request);

    /**
     * 发布成绩
     */
    void publishGrade(Long recordId);

    /**
     * 获取批改结果
     */
    GradingResultResponse getGradingResult(Long recordId);
}

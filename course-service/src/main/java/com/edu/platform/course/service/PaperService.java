package com.edu.platform.course.service;

import com.edu.platform.course.dto.request.ManualPaperRequest;
import com.edu.platform.course.dto.request.RandomPaperRequest;
import com.edu.platform.course.dto.response.PaperResponse;

/**
 * 试卷管理服务
 */
public interface PaperService {

    /**
     * 手动组卷
     */
    Long assembleManualPaper(ManualPaperRequest request);

    /**
     * 随机抽取题目组卷
     */
    Long assembleRandomPaper(RandomPaperRequest request);

    /**
     * 获取试卷详情
     * @param taskId 任务ID
     * @param includeAnswers 是否包含正确答案 (学生答题时应为false)
     */
    PaperResponse getPaperDetail(Long taskId, boolean includeAnswers);

    /**
     * 删除试卷
     */
    void deletePaper(Long taskId);
}

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
    void assembleManualPaper(ManualPaperRequest request);

    /**
     * 随机组卷
     */
    void assembleRandomPaper(RandomPaperRequest request);

    /**
     * 获取试卷详情
     */
    PaperResponse getPaperDetail(Long taskId);

    /**
     * 删除试卷
     */
    void deletePaper(Long taskId);
}

package com.edu.platform.course.service;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.course.dto.response.ExamListResponse;
import com.edu.platform.course.dto.response.PaperResponse;

/**
 * 学生考试服务
 */
public interface StudentExamService {

    /**
     * 获取学生考试列表
     * @param courseId 课程ID(可选)
     * @param status 考试状态(可选): 0-未开始, 1-进行中, 2-已结束
     */
    PageResult<ExamListResponse> getStudentExams(Long courseId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 获取考试详情(学生视角)
     */
    PaperResponse getExamDetail(Long taskId);

    /**
     * 开始考试(创建答题记录)
     * @return 答题记录ID
     */
    Long startExam(Long taskId);
}

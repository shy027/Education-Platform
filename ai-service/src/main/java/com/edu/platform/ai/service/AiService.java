package com.edu.platform.ai.service;

import com.edu.platform.ai.dto.response.AiCourseAnalysisResponse;
import com.edu.platform.ai.dto.response.AiRecommendationResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI 服务接口
 */
public interface AiService {
    
    /**
     * 分析课程文档（PDF/Word）以提取课程信息
     */
    AiCourseAnalysisResponse analyzeCourseDocument(MultipartFile file);

    /**
     * 基于上传的课程文档（PDF/Word）推荐资源库中最相关的内容
     */
    AiRecommendationResponse recommendResourcesByDocument(MultipartFile file);

    /**
     * 基于数据库中现有课程详情推荐资源
     */
    AiRecommendationResponse recommendResources(Long courseId);
}

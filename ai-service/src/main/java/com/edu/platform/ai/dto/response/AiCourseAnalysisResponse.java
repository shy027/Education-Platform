package com.edu.platform.ai.dto.response;

import lombok.Data;
import java.util.List;

/**
 * AI 课程分析结果
 */
@Data
public class AiCourseAnalysisResponse {
    
    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程简介
     */
    private String courseIntro;
    
    /**
     * 学科领域
     */
    private String subjectArea;
    
    /**
     * 建议标签
     */
    private List<String> suggestedTags;
    
    /**
     * 建议素养维度 (如: dimension1, dimension2)
     */
    private List<String> suggestedDimensions;

    /**
     * 关键词
     */
    private List<String> keywords;
}

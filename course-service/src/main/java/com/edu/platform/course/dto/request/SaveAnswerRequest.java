package com.edu.platform.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 保存答案请求
 */
@Data
@Schema(description = "保存答案请求")
public class SaveAnswerRequest {

    @Schema(description = "答题记录ID", required = true)
    private Long recordId;

    @Schema(description = "试卷题目ID (course_task_question.id)", required = true)
    private Long taskQuestionId;

    @Schema(description = "学生答案", required = true)
    private String userAnswer;
}

package com.edu.platform.course.dto.request;

import com.edu.platform.common.dto.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询题目请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "查询题目请求")
public class QuestionQueryRequest extends PageRequest {

    @Schema(description = "课程ID (0表示公共题库)")
    private Long courseId;

    @Schema(description = "章节ID")
    private Long chapterId;

    @Schema(description = "题型: 1-单选 2-多选 3-判断 4-填空 5-简答 6-编程")
    private Integer questionType;

    @Schema(description = "难度: 1-简单 2-中等 3-困难 4-很难 5-极难")
    private Integer difficulty;

    @Schema(description = "关键词搜索")
    private String keyword;

    @Schema(description = "创建人ID")
    private Long creatorId;
}

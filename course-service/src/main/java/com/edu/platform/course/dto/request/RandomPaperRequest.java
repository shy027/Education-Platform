package com.edu.platform.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 随机组卷请求
 */
@Data
@Schema(description = "随机组卷请求")
public class RandomPaperRequest {

    @Schema(description = "任务ID", required = true)
    private Long taskId;

    @Schema(description = "课程ID", required = true)
    private Long courseId;

    @Schema(description = "题型数量配置 (key: 题型1-6, value: 数量)", required = true, example = "{\"1\": 10, \"2\": 5}")
    private Map<Integer, Integer> typeCount;

    @Schema(description = "难度分布配置 (key: 难度1-5, value: 数量)", example = "{\"1\": 5, \"2\": 8, \"3\": 2}")
    private Map<Integer, Integer> difficultyCount;

    @Schema(description = "每题分值 (key: 题型, value: 分值)", example = "{\"1\": 2.0, \"2\": 3.0}")
    private Map<Integer, Double> scorePerType;
}

package com.edu.platform.common.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 课程评分相关元数据DTO
 */
@Data
public class CourseScoringDTO implements Serializable {
    private Long id;
    private String courseName;
    private Long teacherId;
    private String dimensionWeights; // JSON String
    private String scoringConfig;   // JSON String
    private Integer isDimensionLocked;
}

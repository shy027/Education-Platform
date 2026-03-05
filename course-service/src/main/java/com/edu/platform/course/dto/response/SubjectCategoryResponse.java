package com.edu.platform.course.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学科领域分类响应
 */
@Data
public class SubjectCategoryResponse {

    private Long id;

    private String name;

    private Integer sortOrder;

    private Integer isEnabled;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}

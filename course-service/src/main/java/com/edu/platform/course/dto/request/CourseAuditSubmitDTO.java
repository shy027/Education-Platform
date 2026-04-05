package com.edu.platform.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程审核提交请求
 *
 * @author Education Platform
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程审核提交请求")
public class CourseAuditSubmitDTO {

    @Schema(description = "内容类型: COURSE")
    @NotBlank(message = "内容类型不能为空")
    private String contentType;

    @Schema(description = "内容ID")
    @NotNull(message = "内容ID不能为空")
    private Long contentId;
}

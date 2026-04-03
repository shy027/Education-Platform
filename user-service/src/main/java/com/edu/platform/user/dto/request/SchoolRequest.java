package com.edu.platform.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 学校请求对象
 */
@Data
@Schema(description = "学校请求对象")
public class SchoolRequest {

    @Schema(description = "学校名称")
    @NotBlank(message = "学校名称不能为空")
    private String schoolName;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "学校编码")
    private String schoolCode;

    @Schema(description = "学校Logo")
    private String logoUrl;

    @Schema(description = "学校简介")
    @NotBlank(message = "学校简介不能为空")
    private String description;
}

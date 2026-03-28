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
}

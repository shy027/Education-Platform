package com.edu.platform.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批加入请求
 *
 * @author Education Platform
 */
@Data
@Schema(description = "审批加入请求")
public class ApproveJoinRequest {
    
    @Schema(description = "审批状态:1同意,2拒绝", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审批状态不能为空")
    private Integer approveStatus;
}

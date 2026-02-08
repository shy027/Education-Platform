package com.edu.platform.community.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档历史响应
 */
@Data
@Schema(description = "文档历史响应")
public class DocumentHistoryResponse {
    
    @Schema(description = "历史记录ID")
    private Long historyId;
    
    @Schema(description = "文档ID")
    private Long documentId;
    
    @Schema(description = "编辑者ID")
    private Long editorId;
    
    @Schema(description = "编辑者姓名")
    private String editorName;
    
    @Schema(description = "内容快照")
    private String content;
    
    @Schema(description = "版本号")
    private Integer version;
    
    @Schema(description = "编辑时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime editTime;
}

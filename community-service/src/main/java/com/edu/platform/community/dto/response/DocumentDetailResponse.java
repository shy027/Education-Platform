package com.edu.platform.community.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档详情响应
 */
@Data
@Schema(description = "文档详情响应")
public class DocumentDetailResponse {
    
    @Schema(description = "文档ID")
    private Long documentId;
    
    @Schema(description = "小组ID")
    private Long groupId;
    
    @Schema(description = "关联话题ID")
    private Long topicId;
    
    @Schema(description = "文档标题")
    private String title;
    
    @Schema(description = "文档内容(富文本)")
    private String content;
    
    @Schema(description = "版本号")
    private Integer version;
    
    @Schema(description = "最后编辑者ID")
    private Long lastEditorId;
    
    @Schema(description = "最后编辑者姓名")
    private String lastEditorName;
    
    @Schema(description = "最后编辑时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime lastEditTime;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdTime;
}

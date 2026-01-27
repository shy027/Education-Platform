package com.edu.platform.course.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课件响应
 *
 * @author Education Platform
 */
@Data
@Schema(description = "课件响应")
public class CoursewareResponse {
    
    @Schema(description = "课件ID")
    private Long id;
    
    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "章节ID")
    private Long chapterId;
    
    @Schema(description = "章节名称")
    private String chapterName;
    
    @Schema(description = "课件标题")
    private String wareTitle;
    
    @Schema(description = "课件类型: 1-视频 2-文档 3-音频")
    private Integer wareType;
    
    @Schema(description = "文件URL")
    private String fileUrl;
    
    @Schema(description = "文件大小(字节)")
    private Long fileSize;
    
    @Schema(description = "时长(秒,视频/音频)")
    private Integer duration;
    
    @Schema(description = "封面URL")
    private String coverUrl;
    
    @Schema(description = "课件描述")
    private String description;
    
    @Schema(description = "排序号")
    private Integer sortOrder;
    
    @Schema(description = "观看次数")
    private Integer viewCount;
    
    @Schema(description = "下载次数")
    private Integer downloadCount;
    
    @Schema(description = "是否允许下载: 0-否 1-是")
    private Integer allowDownload;
    
    @Schema(description = "审核状态: 0-待审核 1-已通过 2-未通过")
    private Integer auditStatus;
    
    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    
    @Schema(description = "审核人ID")
    private Long auditorId;
    
    @Schema(description = "审核人姓名")
    private String auditorName;
    
    @Schema(description = "创建者ID")
    private Long creatorId;
    
    @Schema(description = "创建者姓名")
    private String creatorName;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}

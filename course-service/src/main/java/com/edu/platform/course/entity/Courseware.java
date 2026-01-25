package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课件表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_courseware")
public class Courseware extends BaseEntity {

    @com.baomidou.mybatisplus.annotation.TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;


    private Long courseId;
    
    private Long chapterId;
    
    private String wareTitle;
    
    /**
     * 类型:1视频,2文档,3PPT,4音频
     */
    private Integer wareType;
    
    private String fileUrl;
    
    private Long fileSize;
    
    /**
     * 时长(秒)
     */
    private Integer duration;
    
    private String coverUrl;
    
    private String description;
    
    private Integer sortOrder;
    
    private Integer viewCount;
    
    private Integer downloadCount;
    
    private Integer allowDownload;
    
    /**
     * 审核状态:0待审核,1通过,2拒绝
     */
    private Integer auditStatus;
    
    private LocalDateTime auditTime;
    
    private Long auditorId;
    
    private Long creatorId;
    
    /**
     * 状态:0隐藏,1发布
     */
    private Integer status;
}

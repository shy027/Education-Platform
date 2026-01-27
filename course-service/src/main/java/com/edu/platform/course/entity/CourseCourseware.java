package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课件实体
 *
 * @author Education Platform
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("course_courseware")
public class CourseCourseware {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 章节ID
     */
    private Long chapterId;
    
    /**
     * 课件标题
     */
    private String wareTitle;
    
    /**
     * 课件类型: 1-视频 2-文档 3-PPT 4-音频
     */
    private Integer wareType;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * 时长(秒,视频/音频)
     */
    private Integer duration;
    
    /**
     * 封面图URL
     */
    private String coverUrl;
    
    /**
     * 课件描述
     */
    private String description;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 允许下载: 0-否 1-是
     */
    private Integer allowDownload;
    
    /**
     * 审核状态: 0-待审核 1-通过 2-拒绝
     */
    private Integer auditStatus;
    
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    
    /**
     * 审核人ID
     */
    private Long auditorId;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 状态: 0-隐藏 1-发布
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
    
    /**
     * 逻辑删除: 0-未删除 1-已删除
     */
    private Integer isDeleted;
}

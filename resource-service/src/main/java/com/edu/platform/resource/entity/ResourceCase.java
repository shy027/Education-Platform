package com.edu.platform.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 智能案例库
 *
 * @author Education Platform
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resource_case")
public class ResourceCase extends BaseEntity {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 案例标题
     */
    private String caseTitle;
    
    /**
     * 案例内容
     */
    private String caseContent;
    
    /**
     * 案例摘要
     */
    private String caseSummary;
    
    /**
     * 学科领域
     */
    private String subjectArea;
    
    /**
     * 思政元素(逗号分隔)
     */
    private String ideologicalElements;
    
    /**
     * 关键词(逗号分隔)
     */
    private String keywords;
    
    /**
     * 难度 (1:简单 2:中等 3:困难)
     */
    private Integer difficultyLevel;
    
    /**
     * 适用年级
     */
    private String suitableGrade;
    
    /**
     * 附件URLs(JSON数组)
     */
    private String attachmentUrls;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 使用次数
     */
    private Integer useCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 来源 (1:平台原创 2:外部导入)
     */
    private Integer sourceType;
    
    /**
     * 来源链接
     */
    private String sourceUrl;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 审核状态 (0:待审核 1:通过 2:拒绝)
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
     * 审核备注
     */
    private String auditRemark;
    
    /**
     * 状态 (0:下架 1:上架)
     */
    private Integer status;
    
}

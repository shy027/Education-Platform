package com.edu.platform.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 案例标签关联表
 *
 * @author Education Platform
 */
@Data
@TableName("resource_case_rel_tag")
public class ResourceCaseRelTag {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 案例ID
     */
    private Long caseId;
    
    /**
     * 标签ID
     */
    private Long tagId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
}

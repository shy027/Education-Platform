package com.edu.platform.resource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源分类表
 *
 * @author Education Platform
 */
@Data
@TableName("resource_category")
public class ResourceCategory {
    
    /**
     * 分类ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 父分类ID, 0表示顶级
     */
    private Long parentId;
    
    /**
     * 层级: 1-一级, 2-二级
     */
    private Integer level;
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
    
    /**
     * 分类图标
     */
    private String icon;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    /**
     * 逻辑删除标志 (0:未删除 1:已删除)
     */
    @TableLogic
    private Integer isDeleted;
    
}

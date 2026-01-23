package com.edu.platform.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 思政元素标签表
 *
 * @author Education Platform
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resource_tag")
public class ResourceTag extends BaseEntity {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 标签名称
     */
    private String tagName;
    
    /**
     * 标签颜色(十六进制,如:#409EFF)
     */
    private String tagColor;
    
    /**
     * 标签分类(已废弃,使用categoryId)
     */
    @Deprecated
    @TableField(exist = false)
    private String tagCategory;
    
    /**
     * 所属分类ID
     */
    private Long categoryId;
    
    /**
     * 标签描述
     */
    private String description;
    
    /**
     * 使用次数
     */
    private Integer useCount;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 状态 (0:禁用 1:启用)
     */
    private Integer status;
    
}

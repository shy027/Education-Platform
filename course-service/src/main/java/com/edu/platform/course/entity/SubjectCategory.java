package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 学科领域分类实体类
 */
@Data
@TableName("subject_category")
public class SubjectCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学科名称
     */
    private String name;

    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer isEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}

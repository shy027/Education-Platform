package com.edu.platform.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校表
 *
 * @author Education Platform
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_school")
public class UserSchool extends BaseEntity {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学校编码(唯一)
     */
    private String schoolCode;
    
    /**
     * 学校名称
     */
    private String schoolName;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 详细地址
     */
    private String address;
    
    /**
     * 学校LOGO
     */
    private String logoUrl;
    
    /**
     * 学校简介
     */
    private String description;
    
    /**
     * 联系人
     */
    private String contactPerson;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 状态 (0:禁用 1:正常)
     */
    private Integer status;
    
    /**
     * 创建人ID
     */
    private Long createdBy;
    
}

package com.edu.platform.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学习行为埋点实体
 *
 * @author Education Platform
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report_behavior_log")
public class BehaviorLog extends BaseEntity {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 课程ID
     */
    @TableField("course_id")
    private Long courseId;
    
    /**
     * 行为类型
     */
    @TableField("behavior_type")
    private String behaviorType;
    
    /**
     * 对象类型
     */
    @TableField("behavior_object_type")
    private String behaviorObjectType;
    
    /**
     * 对象ID
     */
    @TableField("behavior_object_id")
    private Long behaviorObjectId;
    
    /**
     * 行为数据(JSON)
     */
    @TableField("behavior_data")
    private String behaviorData;
    
    /**
     * 持续时长(秒)
     */
    @TableField("duration_seconds")
    private Integer durationSeconds;
    
    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;
    
    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;
    
}

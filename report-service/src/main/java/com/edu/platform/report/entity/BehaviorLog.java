package com.edu.platform.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    private Long userId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 行为类型
     */
    private String behaviorType;
    
    /**
     * 对象类型
     */
    private String behaviorObjectType;
    
    /**
     * 对象ID
     */
    private Long behaviorObjectId;
    
    /**
     * 行为数据(JSON)
     */
    private String behaviorData;
    
    /**
     * 持续时长(秒)
     */
    private Integer durationSeconds;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
}

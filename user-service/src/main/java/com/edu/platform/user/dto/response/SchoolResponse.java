package com.edu.platform.user.dto.response;

import lombok.Data;

/**
 * 学校响应
 *
 * @author Education Platform
 */
@Data
public class SchoolResponse {
    
    /**
     * 学校ID
     */
    private Long schoolId;
    
    /**
     * 学校编码
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
     * 教师数量
     */
    private Integer teacherCount;
    
    /**
     * 学生数量
     */
    private Integer studentCount;
    
    /**
     * 课程数量
     */
    private Integer courseCount;
    
}

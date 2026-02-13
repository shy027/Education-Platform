package com.edu.platform.report.dto;

import lombok.Data;

/**
 * 配置DTO
 *
 * @author Education Platform
 */
@Data
public class ConfigDTO {
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置类型
     */
    private String configType;
    
    /**
     * 描述
     */
    private String description;
}

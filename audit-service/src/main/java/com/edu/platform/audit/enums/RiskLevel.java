package com.edu.platform.audit.enums;

import lombok.Getter;

/**
 * 风险等级枚举
 *
 * @author Education Platform
 */
@Getter
public enum RiskLevel {
    
    LOW(1, "低风险"),
    MEDIUM(2, "中风险"),
    HIGH(3, "高风险");
    
    private final Integer code;
    private final String name;
    
    RiskLevel(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据code获取枚举
     */
    public static RiskLevel fromCode(Integer code) {
        for (RiskLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("未知的风险等级: " + code);
    }
}

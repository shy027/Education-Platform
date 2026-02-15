package com.edu.platform.audit.enums;

import lombok.Getter;

/**
 * 审核方式枚举
 *
 * @author Education Platform
 */
@Getter
public enum AuditMethod {
    
    AI(1, "AI审核"),
    MANUAL(2, "人工审核");
    
    private final Integer code;
    private final String name;
    
    AuditMethod(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据code获取枚举
     */
    public static AuditMethod fromCode(Integer code) {
        for (AuditMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("未知的审核方式: " + code);
    }
}

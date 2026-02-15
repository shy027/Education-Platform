package com.edu.platform.audit.enums;

import lombok.Getter;

/**
 * 审核结果枚举
 *
 * @author Education Platform
 */
@Getter
public enum AuditResult {
    
    PENDING(0, "待审核"),
    APPROVED(1, "通过"),
    REJECTED(2, "拒绝");
    
    private final Integer code;
    private final String name;
    
    AuditResult(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据code获取枚举
     */
    public static AuditResult fromCode(Integer code) {
        for (AuditResult result : values()) {
            if (result.code.equals(code)) {
                return result;
            }
        }
        throw new IllegalArgumentException("未知的审核结果: " + code);
    }
}

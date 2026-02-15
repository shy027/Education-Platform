package com.edu.platform.audit.enums;

import lombok.Getter;

/**
 * 内容类型枚举
 *
 * @author Education Platform
 */
@Getter
public enum ContentType {
    
    COURSEWARE("COURSEWARE", "课件"),
    POST("POST", "帖子"),
    COMMENT("COMMENT", "评论");
    
    private final String code;
    private final String name;
    
    ContentType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据code获取枚举
     */
    public static ContentType fromCode(String code) {
        for (ContentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的内容类型: " + code);
    }
}

package com.edu.platform.report.constant;

/**
 * 报告状态枚举
 *
 * @author Education Platform
 */
public enum ReportStatusEnum {
    
    /**
     * 等待生成
     */
    PENDING("PENDING", "等待生成"),
    
    /**
     * 生成中
     */
    PROCESSING("PROCESSING", "生成中"),
    
    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成"),
    
    /**
     * 生成失败
     */
    FAILED("FAILED", "生成失败");
    
    private final String code;
    private final String desc;
    
    ReportStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
}

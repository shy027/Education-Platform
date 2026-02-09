package com.edu.platform.report.constant;

/**
 * 行为类型枚举
 *
 * @author Education Platform
 */
public enum BehaviorTypeEnum {
    
    /**
     * 浏览课件
     */
    VIEW_COURSEWARE("VIEW_COURSEWARE", "浏览课件"),
    
    /**
     * 完成课件
     */
    COMPLETE_COURSEWARE("COMPLETE_COURSEWARE", "完成课件"),
    
    /**
     * 提交任务
     */
    SUBMIT_TASK("SUBMIT_TASK", "提交任务"),
    
    /**
     * 发布讨论
     */
    CREATE_POST("CREATE_POST", "发布讨论"),
    
    /**
     * 发表评论
     */
    CREATE_COMMENT("CREATE_COMMENT", "发表评论"),
    
    /**
     * 点赞帖子
     */
    LIKE_POST("LIKE_POST", "点赞帖子"),
    
    /**
     * 浏览案例
     */
    VIEW_CASE("VIEW_CASE", "浏览案例"),
    
    /**
     * 登录系统
     */
    LOGIN("LOGIN", "登录系统");
    
    private final String code;
    private final String desc;
    
    BehaviorTypeEnum(String code, String desc) {
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

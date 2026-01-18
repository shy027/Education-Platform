package com.edu.platform.common.constant;

/**
 * 系统常量
 *
 * @author Education Platform
 */
public class Constants {
    
    /**
     * UTF-8编码
     */
    public static final String UTF8 = "UTF-8";
    
    /**
     * Token请求头
     */
    public static final String AUTHORIZATION = "Authorization";
    
    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * 用户ID键
     */
    public static final String USER_ID = "userId";
    
    /**
     * 用户名键
     */
    public static final String USERNAME = "username";
    
    /**
     * Redis键前缀
     */
    public static final String REDIS_KEY_PREFIX = "edu:platform:";
    
    /**
     * 用户信息缓存键
     */
    public static final String REDIS_USER_KEY = REDIS_KEY_PREFIX + "user:";
    
    /**
     * 验证码缓存键
     */
    public static final String REDIS_CAPTCHA_KEY = REDIS_KEY_PREFIX + "captcha:";
    
    /**
     * 缓存过期时间 (30分钟)
     */
    public static final long CACHE_EXPIRE_TIME = 30 * 60;
    
    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;
    
    /**
     * 默认每页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;
    
    /**
     * 最大每页大小
     */
    public static final int MAX_PAGE_SIZE = 100;
    
}

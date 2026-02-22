package com.edu.platform.gateway.exception;

/**
 * Token 相关异常
 *
 * @author Education Platform
 */
public class TokenException extends RuntimeException {

    private final int code;

    public TokenException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * Token 缺失（未携带）
     */
    public static TokenException missing() {
        return new TokenException(401, "未携带Token，请先登录");
    }

    /**
     * Token 无效（格式错误 / 签名错误）
     */
    public static TokenException invalid() {
        return new TokenException(401, "Token无效");
    }

    /**
     * Token 过期
     */
    public static TokenException expired() {
        return new TokenException(401, "Token已过期，请重新登录");
    }
}

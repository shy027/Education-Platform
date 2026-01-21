package com.edu.platform.common.result;

import lombok.Getter;

/**
 * 响应状态码枚举
 *
 * @author Education Platform
 */
@Getter
public enum ResultCode {
    
    /**
     * 成功
     */
    SUCCESS(200, "success"),
    
    /**
     * 失败
     */
    FAIL(400, "fail"),
    
    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权"),
    
    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),
    
    /**
     * 服务器错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器错误"),
    
    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),
    
    /**
     * 用户名或密码错误
     */
    USERNAME_OR_PASSWORD_ERROR(400, "用户名或密码错误"),
    
    /**
     * Token无效
     */
    TOKEN_INVALID(401, "Token无效"),
    
    /**
     * Token过期
     */
    TOKEN_EXPIRED(401, "Token已过期"),
    
    /**
     * 用户已存在
     */
    USER_ALREADY_EXISTS(400, "用户已存在"),
    
    /**
     * 用户不存在
     */
    USER_NOT_FOUND(404, "用户不存在"),
    
    /**
     * 数据已存在
     */
    DATA_ALREADY_EXISTS(400, "数据已存在"),
    
    /**
     * 数据不存在
     */
    DATA_NOT_FOUND(404, "数据不存在"),
    
    /**
     * 操作失败
     */
    OPERATION_FAILED(400, "操作失败"),
    
    /**
     * 系统错误
     */
    SYSTEM_ERROR(500, "系统错误");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
}

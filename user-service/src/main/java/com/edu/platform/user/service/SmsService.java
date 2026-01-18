package com.edu.platform.user.service;

/**
 * 短信服务接口
 *
 * @author Education Platform
 */
public interface SmsService {
    
    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @return 验证码(开发模式返回,生产模式返回null)
     */
    String sendVerifyCode(String phone);
    
    /**
     * 验证验证码
     *
     * @param phone 手机号
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyCode(String phone, String code);
    
}

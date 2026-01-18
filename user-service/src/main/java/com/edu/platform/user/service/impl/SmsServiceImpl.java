package com.edu.platform.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.user.config.RongLianProperties;
import com.edu.platform.user.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {
    
    private final RongLianProperties rongLianProperties;
    private final StringRedisTemplate stringRedisTemplate;
    
    private static final String VERIFY_CODE_PREFIX = "sms:verify:code:";
    private static final String SEND_LIMIT_PREFIX = "sms:send:limit:";
    private static final int CODE_LENGTH = 6;
    private static final long CODE_EXPIRE_MINUTES = 5;
    private static final long SEND_INTERVAL_SECONDS = 60;
    
    @Override
    public String sendVerifyCode(String phone) {
        // 参数校验
        if (StrUtil.isBlank(phone)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "手机号不能为空");
        }
        
        // 检查发送频率限制(60秒内不可重复发送)
        String limitKey = SEND_LIMIT_PREFIX + phone;
        String limitValue = stringRedisTemplate.opsForValue().get(limitKey);
        if (StrUtil.isNotBlank(limitValue)) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "验证码发送过于频繁,请60秒后再试");
        }
        
        // 生成6位数字验证码
        String code = RandomUtil.randomNumbers(CODE_LENGTH);
        
        // 存储到Redis
        String key = VERIFY_CODE_PREFIX + phone;
        stringRedisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 设置发送频率限制(60秒)
        stringRedisTemplate.opsForValue().set(limitKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
        
        // 开发模式:直接返回验证码
        if (Boolean.TRUE.equals(rongLianProperties.getDevMode())) {
            log.info("【开发模式】手机号: {}, 验证码: {}, 有效期: {}分钟", phone, code, CODE_EXPIRE_MINUTES);
            return code;
        }
        
        // TODO: 生产模式需要集成容联云SDK发送短信
        // 目前暂时也返回验证码用于测试
        log.warn("【生产模式-未实现】手机号: {}, 验证码: {}", phone, code);
        return code;
    }
    
    @Override
    public boolean verifyCode(String phone, String code) {
        if (StrUtil.isBlank(phone) || StrUtil.isBlank(code)) {
            return false;
        }
        
        String key = VERIFY_CODE_PREFIX + phone;
        String storedCode = stringRedisTemplate.opsForValue().get(key);
        
        if (StrUtil.isBlank(storedCode)) {
            return false;
        }
        
        // 验证成功后删除验证码
        if (code.equals(storedCode)) {
            stringRedisTemplate.delete(key);
            return true;
        }
        
        return false;
    }
    
}

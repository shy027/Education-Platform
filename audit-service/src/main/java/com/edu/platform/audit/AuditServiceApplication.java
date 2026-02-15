package com.edu.platform.audit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 审核服务启动类
 *
 * @author Education Platform
 */
@SpringBootApplication
@EnableFeignClients
@MapperScan("com.edu.platform.audit.mapper")
@ComponentScan(basePackages = {
    "com.edu.platform.audit",
    "com.edu.platform.common"  // 扫描common包,使全局异常处理器生效
})
public class AuditServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuditServiceApplication.class, args);
        System.out.println("审核服务启动成功! 端口: 8086");
    }
    
}

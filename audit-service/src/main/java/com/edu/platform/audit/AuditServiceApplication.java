package com.edu.platform.audit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.edu.platform.audit.mapper")
public class AuditServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuditServiceApplication.class, args);
        System.out.println("审核服务启动成功! 端口: 8086");
    }
    
}

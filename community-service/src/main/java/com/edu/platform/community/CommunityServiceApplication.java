package com.edu.platform.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.edu.platform.community.mapper")
public class CommunityServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CommunityServiceApplication.class, args);
        System.out.println("社区服务启动成功! 端口: 8084");
    }
    
}

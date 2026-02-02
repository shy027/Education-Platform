package com.edu.platform.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 社区服务启动类
 *
 * @author Education Platform
 */
@SpringBootApplication
@MapperScan("com.edu.platform.community.mapper")
@ComponentScan(basePackages = {"com.edu.platform.community", "com.edu.platform.common"})
@EnableFeignClients
public class CommunityServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CommunityServiceApplication.class, args);
        System.out.println("社区服务启动成功! 端口: 8084");
    }
    
}

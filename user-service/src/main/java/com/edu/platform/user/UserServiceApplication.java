package com.edu.platform.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户服务启动类
 *
 * @author Education Platform
 */
@SpringBootApplication
@MapperScan("com.edu.platform.user.mapper")
@ComponentScan(basePackages = {"com.edu.platform.user", "com.edu.platform.common"})
public class UserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("用户服务启动成功! 端口: 8081");
    }
    
}

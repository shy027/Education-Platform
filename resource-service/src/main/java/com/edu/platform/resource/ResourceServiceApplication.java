package com.edu.platform.resource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.edu.platform.resource.mapper")
public class ResourceServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ResourceServiceApplication.class, args);
        System.out.println("资源服务启动成功! 端口: 8082");
    }
    
}

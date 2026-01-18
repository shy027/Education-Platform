package com.edu.platform.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.edu.platform.report.mapper")
public class ReportServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ReportServiceApplication.class, args);
        System.out.println("报告服务启动成功! 端口: 8085");
    }
    
}

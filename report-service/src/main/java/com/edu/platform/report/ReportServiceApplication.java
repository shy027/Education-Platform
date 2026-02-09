package com.edu.platform.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 报告服务启动类
 *
 * @author Education Platform
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.edu.platform.report", "com.edu.platform.common"})
@EnableAsync
@EnableScheduling
public class ReportServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ReportServiceApplication.class, args);
    }
    
}

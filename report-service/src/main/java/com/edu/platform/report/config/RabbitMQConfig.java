package com.edu.platform.report.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置
 *
 * @author Education Platform
 */
@Configuration
public class RabbitMQConfig {
    
    /**
     * 报告生成队列
     */
    public static final String REPORT_QUEUE = "report.generate.queue";
    
    /**
     * 报告生成交换机
     */
    public static final String REPORT_EXCHANGE = "report.generate.exchange";
    
    /**
     * 报告生成路由键
     */
    public static final String REPORT_ROUTING_KEY = "report.generate";
    
    @Bean
    public Queue reportQueue() {
        return QueueBuilder.durable(REPORT_QUEUE).build();
    }
    
    @Bean
    public DirectExchange reportExchange() {
        return new DirectExchange(REPORT_EXCHANGE);
    }
    
    @Bean
    public Binding reportBinding() {
        return BindingBuilder
                .bind(reportQueue())
                .to(reportExchange())
                .with(REPORT_ROUTING_KEY);
    }
    
}

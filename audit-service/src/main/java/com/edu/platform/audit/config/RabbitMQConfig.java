package com.edu.platform.audit.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置 - audit-service
 *
 * @author Education Platform
 */
@Configuration
public class RabbitMQConfig {

    /** 审核请求队列 */
    public static final String AUDIT_REQUEST_QUEUE = "audit.request.queue";

    /** 审核请求交换机 */
    public static final String AUDIT_REQUEST_EXCHANGE = "audit.request.exchange";

    /** 审核请求路由键 */
    public static final String AUDIT_REQUEST_ROUTING_KEY = "audit.request";

    /**
     * 审核请求队列 (持久化)
     */
    @Bean
    public Queue auditRequestQueue() {
        return QueueBuilder.durable(AUDIT_REQUEST_QUEUE).build();
    }

    /**
     * 审核请求交换机 (直连)
     */
    @Bean
    public DirectExchange auditRequestExchange() {
        return new DirectExchange(AUDIT_REQUEST_EXCHANGE, true, false);
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding auditRequestBinding() {
        return BindingBuilder
                .bind(auditRequestQueue())
                .to(auditRequestExchange())
                .with(AUDIT_REQUEST_ROUTING_KEY);
    }

    /**
     * 使用JSON序列化消息
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate使用JSON转换器
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

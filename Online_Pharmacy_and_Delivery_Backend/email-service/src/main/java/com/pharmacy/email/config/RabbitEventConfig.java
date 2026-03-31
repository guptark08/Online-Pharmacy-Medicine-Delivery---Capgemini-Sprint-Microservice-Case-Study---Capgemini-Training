package com.pharmacy.email.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class RabbitEventConfig {

    @Value("${pharmacy.events.exchange:pharmacy.events.exchange}")
    private String exchangeName;

    @Value("${pharmacy.events.queues.email-verification:pharmacy.email.verification}")
    private String emailVerificationQueue;

    @Value("${pharmacy.events.queues.login-alert:pharmacy.email.login-alert}")
    private String loginAlertQueue;

    @Value("${pharmacy.events.queues.otp-delivery:pharmacy.email.otp}")
    private String otpDeliveryQueue;

    @Value("${pharmacy.events.queues.password-reset:pharmacy.email.password-reset}")
    private String passwordResetQueue;

    @Bean
    public TopicExchange pharmacyEventsExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue emailVerificationQueue() {
        return QueueBuilder.durable(emailVerificationQueue).build();
    }

    @Bean
    public Queue loginAlertQueue() {
        return QueueBuilder.durable(loginAlertQueue).build();
    }

    @Bean
    public Queue otpDeliveryQueue() {
        return QueueBuilder.durable(otpDeliveryQueue).build();
    }

    @Bean
    public Queue passwordResetQueue() {
        return QueueBuilder.durable(passwordResetQueue).build();
    }

    @Bean
    public Declarables emailEventTopology(TopicExchange pharmacyEventsExchange) {
        return new Declarables(
            BindingBuilder.bind(emailVerificationQueue()).to(pharmacyEventsExchange).with("auth.email.verification"),
            BindingBuilder.bind(loginAlertQueue()).to(pharmacyEventsExchange).with("auth.email.login-alert"),
            BindingBuilder.bind(otpDeliveryQueue()).to(pharmacyEventsExchange).with("auth.otp.delivery"),
            BindingBuilder.bind(passwordResetQueue()).to(pharmacyEventsExchange).with("auth.password.reset")
        );
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setPrefetchCount(10);
        return factory;
    }
}

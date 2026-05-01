package com.orderanddelivery.messaging;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitEventConfig {

    @Bean
    public TopicExchange pharmacyEventsExchange(
            @Value("${pharmacy.events.exchange:" + PharmacyEventRoutingKeys.EXCHANGE + "}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue prescriptionReviewedQueue(
            @Value("${pharmacy.events.queues.prescription-reviewed:pharmacy.order.prescription-reviewed}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding prescriptionReviewedBinding(
            Queue prescriptionReviewedQueue,
            TopicExchange pharmacyEventsExchange) {
        return BindingBuilder.bind(prescriptionReviewedQueue)
                .to(pharmacyEventsExchange)
                .with(PharmacyEventRoutingKeys.PRESCRIPTION_REVIEWED);
    }

    @Bean
    @ConditionalOnProperty(prefix = "pharmacy.events.rabbit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AmqpAdmin pharmacyAmqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    @ConditionalOnProperty(prefix = "pharmacy.events.rabbit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ApplicationRunner declareOrderEventTopology(
            AmqpAdmin amqpAdmin,
            TopicExchange pharmacyEventsExchange,
            Queue prescriptionReviewedQueue,
            Binding prescriptionReviewedBinding) {
        return args -> {
            amqpAdmin.declareExchange(pharmacyEventsExchange);
            amqpAdmin.declareQueue(prescriptionReviewedQueue);
            amqpAdmin.declareBinding(prescriptionReviewedBinding);
        };
    }
}

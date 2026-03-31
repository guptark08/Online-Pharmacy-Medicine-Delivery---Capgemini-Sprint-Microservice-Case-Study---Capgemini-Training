package com.orderanddelivery.messaging;

import org.springframework.amqp.core.AmqpAdmin;
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
    @ConditionalOnProperty(prefix = "pharmacy.events.rabbit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AmqpAdmin pharmacyAmqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    @ConditionalOnProperty(prefix = "pharmacy.events.rabbit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ApplicationRunner declareOrderEventExchange(AmqpAdmin amqpAdmin, TopicExchange pharmacyEventsExchange) {
        return args -> amqpAdmin.declareExchange(pharmacyEventsExchange);
    }
}

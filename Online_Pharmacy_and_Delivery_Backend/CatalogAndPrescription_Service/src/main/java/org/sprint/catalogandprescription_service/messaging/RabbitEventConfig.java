package org.sprint.catalogandprescription_service.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitEventConfig {

    @Bean
    public TopicExchange pharmacyEventsExchange(
            @Value("${pharmacy.events.exchange:" + PharmacyEventRoutingKeys.EXCHANGE + "}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }
}

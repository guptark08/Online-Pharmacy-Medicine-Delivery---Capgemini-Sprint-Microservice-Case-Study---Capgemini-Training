package com.pharmacy.admin.messaging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
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
    public DirectExchange pharmacyEventsDeadLetterExchange(
            @Value("${pharmacy.events.dlx:pharmacy.events.dlx}") String dlxName) {
        return new DirectExchange(dlxName, true, false);
    }

    @Bean
    @ConditionalOnProperty(prefix = "pharmacy.events.rabbit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AmqpAdmin pharmacyAmqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Declarables adminEventTopology(
            TopicExchange pharmacyEventsExchange,
            DirectExchange pharmacyEventsDeadLetterExchange,
            @Value("${pharmacy.events.queues.order-created:pharmacy.admin.order-created}") String orderCreatedQueue,
            @Value("${pharmacy.events.queues.payment-succeeded:pharmacy.admin.payment-succeeded}") String paymentSucceededQueue,
            @Value("${pharmacy.events.queues.payment-failed:pharmacy.admin.payment-failed}") String paymentFailedQueue,
            @Value("${pharmacy.events.queues.prescription-reviewed:pharmacy.admin.prescription-reviewed}") String prescriptionReviewedQueue,
            @Value("${pharmacy.events.queues.order-status-changed:pharmacy.admin.order-status-changed}") String orderStatusChangedQueue,
            @Value("${pharmacy.events.queues.inventory-adjusted:pharmacy.admin.inventory-adjusted}") String inventoryAdjustedQueue,
            @Value("${pharmacy.events.dlq.suffix:.dlq}") String dlqSuffix) {

        List<Declarable> declarations = new ArrayList<>();

        declarations.addAll(buildQueueWithDlq(
                orderCreatedQueue,
                PharmacyEventRoutingKeys.ORDER_CREATED,
                pharmacyEventsExchange,
                pharmacyEventsDeadLetterExchange,
                dlqSuffix));

        declarations.addAll(buildQueueWithDlq(
                paymentSucceededQueue,
                PharmacyEventRoutingKeys.PAYMENT_SUCCEEDED,
                pharmacyEventsExchange,
                pharmacyEventsDeadLetterExchange,
                dlqSuffix));

        declarations.addAll(buildQueueWithDlq(
                paymentFailedQueue,
                PharmacyEventRoutingKeys.PAYMENT_FAILED,
                pharmacyEventsExchange,
                pharmacyEventsDeadLetterExchange,
                dlqSuffix));

        declarations.addAll(buildQueueWithDlq(
                prescriptionReviewedQueue,
                PharmacyEventRoutingKeys.PRESCRIPTION_REVIEWED,
                pharmacyEventsExchange,
                pharmacyEventsDeadLetterExchange,
                dlqSuffix));

        declarations.addAll(buildQueueWithDlq(
                orderStatusChangedQueue,
                PharmacyEventRoutingKeys.ORDER_STATUS_CHANGED,
                pharmacyEventsExchange,
                pharmacyEventsDeadLetterExchange,
                dlqSuffix));

        declarations.addAll(buildQueueWithDlq(
                inventoryAdjustedQueue,
                PharmacyEventRoutingKeys.INVENTORY_ADJUSTED,
                pharmacyEventsExchange,
                pharmacyEventsDeadLetterExchange,
                dlqSuffix));

        return new Declarables(declarations);
    }

    @Bean
    @ConditionalOnProperty(prefix = "pharmacy.events.rabbit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ApplicationRunner declareAdminEventTopology(
            AmqpAdmin amqpAdmin,
            TopicExchange pharmacyEventsExchange,
            DirectExchange pharmacyEventsDeadLetterExchange,
            Declarables adminEventTopology) {
        return args -> {
            amqpAdmin.declareExchange(pharmacyEventsExchange);
            amqpAdmin.declareExchange(pharmacyEventsDeadLetterExchange);
            for (Declarable declarable : adminEventTopology.getDeclarables()) {
                if (declarable instanceof Queue queue) {
                    amqpAdmin.declareQueue(queue);
                } else if (declarable instanceof Binding binding) {
                    amqpAdmin.declareBinding(binding);
                } else if (declarable instanceof TopicExchange topicExchange) {
                    amqpAdmin.declareExchange(topicExchange);
                } else if (declarable instanceof DirectExchange directExchange) {
                    amqpAdmin.declareExchange(directExchange);
                }
            }
        };
    }

    private List<Declarable> buildQueueWithDlq(
            String queueName,
            String routingKey,
            TopicExchange mainExchange,
            DirectExchange deadLetterExchange,
            String dlqSuffix) {

        String suffix = (dlqSuffix == null || dlqSuffix.isBlank()) ? ".dlq" : dlqSuffix.trim();
        String deadLetterQueueName = queueName + suffix;

        Queue mainQueue = QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchange.getName())
                .withArgument("x-dead-letter-routing-key", deadLetterQueueName)
                .build();

        Queue deadLetterQueue = QueueBuilder.durable(deadLetterQueueName).build();

        Binding mainBinding = BindingBuilder.bind(mainQueue).to(mainExchange).with(routingKey);
        Binding deadLetterBinding = BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(deadLetterQueueName);

        return List.of(mainQueue, deadLetterQueue, mainBinding, deadLetterBinding);
    }
}

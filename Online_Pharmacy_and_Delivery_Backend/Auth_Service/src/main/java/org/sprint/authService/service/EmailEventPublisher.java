package org.sprint.authService.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.sprint.authService.messaging.AuthEventRoutingKeys;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${pharmacy.events.exchange:pharmacy.events.exchange}")
    private String exchangeName;

    @Value("${pharmacy.events.rabbit.enabled:true}")
    private boolean rabbitEnabled;

    public void publishEmailVerificationEvent(Object event) {
        if (!rabbitEnabled) return;
        try {
            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchangeName, AuthEventRoutingKeys.EMAIL_VERIFICATION, json);
            log.debug("Published email verification event to exchange: {}", exchangeName);
        } catch (Exception e) {
            log.error("Failed to publish email verification event: {}", e.getMessage(), e);
        }
    }

    public void publishLoginAlertEvent(Object event) {
        if (!rabbitEnabled) return;
        try {
            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchangeName, AuthEventRoutingKeys.LOGIN_ALERT, json);
            log.debug("Published login alert event to exchange: {}", exchangeName);
        } catch (Exception e) {
            log.error("Failed to publish login alert event: {}", e.getMessage(), e);
        }
    }

    public void publishOtpDeliveryEvent(Object event) {
        if (!rabbitEnabled) return;
        try {
            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchangeName, AuthEventRoutingKeys.OTP_DELIVERY, json);
            log.debug("Published OTP delivery event to exchange: {}", exchangeName);
        } catch (Exception e) {
            log.error("Failed to publish OTP delivery event: {}", e.getMessage(), e);
        }
    }

    public void publishPasswordResetEvent(Object event) {
        if (!rabbitEnabled) return;
        try {
            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchangeName, AuthEventRoutingKeys.PASSWORD_RESET, json);
            log.debug("Published password reset event to exchange: {}", exchangeName);
        } catch (Exception e) {
            log.error("Failed to publish password reset event: {}", e.getMessage(), e);
        }
    }
}

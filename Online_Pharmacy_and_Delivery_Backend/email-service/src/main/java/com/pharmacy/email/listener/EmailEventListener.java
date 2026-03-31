package com.pharmacy.email.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.pharmacy.email.dto.EmailVerificationEvent;
import com.pharmacy.email.dto.LoginAlertEvent;
import com.pharmacy.email.dto.OtpDeliveryEvent;
import com.pharmacy.email.dto.PasswordResetEvent;
import com.pharmacy.email.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventListener {

    private final EmailSenderService emailSenderService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${pharmacy.events.queues.email-verification:pharmacy.email.verification}")
    public void handleEmailVerification(String payload) throws JsonProcessingException {
        log.info("Received email verification event");
        EmailVerificationEvent event = objectMapper.readValue(payload, EmailVerificationEvent.class);
        log.info("Processing email verification for user: {}, email: {}", event.getUserId(), event.getEmail());
        emailSenderService.sendEmailVerification(event);
        log.info("Successfully processed email verification for user: {}", event.getUserId());
    }

    @RabbitListener(queues = "${pharmacy.events.queues.login-alert:pharmacy.email.login-alert}")
    public void handleLoginAlert(String payload) throws JsonProcessingException {
        log.info("Received login alert event");
        LoginAlertEvent event = objectMapper.readValue(payload, LoginAlertEvent.class);
        log.info("Processing login alert for user: {}, email: {}", event.getUserId(), event.getEmail());
        emailSenderService.sendLoginAlert(event);
        log.info("Successfully processed login alert for user: {}", event.getUserId());
    }

    @RabbitListener(queues = "${pharmacy.events.queues.otp-delivery:pharmacy.email.otp}")
    public void handleOtpDelivery(String payload) throws JsonProcessingException {
        log.info("Received OTP delivery event");
        OtpDeliveryEvent event = objectMapper.readValue(payload, OtpDeliveryEvent.class);
        log.info("Processing OTP delivery for user: {}, email: {}", event.getUserId(), event.getEmail());
        emailSenderService.sendOtpEmail(event);
        log.info("Successfully processed OTP delivery for user: {}", event.getUserId());
    }

    @RabbitListener(queues = "${pharmacy.events.queues.password-reset:pharmacy.email.password-reset}")
    public void handlePasswordReset(String payload) throws JsonProcessingException {
        log.info("Received password reset event");
        PasswordResetEvent event = objectMapper.readValue(payload, PasswordResetEvent.class);
        log.info("Processing password reset for user: {}, email: {}", event.getUserId(), event.getEmail());
        emailSenderService.sendPasswordResetEmail(event);
        log.info("Successfully processed password reset for user: {}", event.getUserId());
    }
}

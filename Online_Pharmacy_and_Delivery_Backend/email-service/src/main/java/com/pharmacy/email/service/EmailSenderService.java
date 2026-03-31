package com.pharmacy.email.service;

import com.pharmacy.email.dto.EmailVerificationEvent;
import com.pharmacy.email.dto.LoginAlertEvent;
import com.pharmacy.email.dto.OtpDeliveryEvent;
import com.pharmacy.email.dto.PasswordResetEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from-name:Pharmacy System}")
    private String fromName;

    @Value("${app.email.from-address:noreply@pharmacy.com}")
    private String fromAddress;

    public void sendEmailVerification(EmailVerificationEvent event) {
        log.info("Sending email verification to: {}", event.getEmail());
        try {
            String subject = "Verify Your Email Address - Pharmacy System";
            String body = buildEmailVerificationBody(event);
            sendHtmlEmail(event.getEmail(), subject, body);
            log.info("Email verification sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", event.getEmail(), e);
        }
    }

    public void sendLoginAlert(LoginAlertEvent event) {
        log.info("Sending login alert to: {}", event.getEmail());
        try {
            String subject = "New Login to Your Pharmacy Account";
            String body = buildLoginAlertBody(event);
            sendHtmlEmail(event.getEmail(), subject, body);
            log.info("Login alert sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send login alert to: {}", event.getEmail(), e);
        }
    }

    public void sendOtpEmail(OtpDeliveryEvent event) {
        log.info("Sending OTP email to: {}", event.getEmail());
        try {
            String subject = "Your OTP Code - Pharmacy System";
            String body = buildOtpEmailBody(event);
            sendHtmlEmail(event.getEmail(), subject, body);
            log.info("OTP email sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", event.getEmail(), e);
        }
    }

    public void sendPasswordResetEmail(PasswordResetEvent event) {
        log.info("Sending password reset email to: {}", event.getEmail());
        try {
            String subject = "Reset Your Password - Pharmacy System";
            String body = buildPasswordResetBody(event);
            sendHtmlEmail(event.getEmail(), subject, body);
            log.info("Password reset email sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", event.getEmail(), e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    private String buildEmailVerificationBody(EmailVerificationEvent event) {
        return "<html><body>" +
            "<h2>Verify Your Email Address</h2>" +
            "<p>Hello " + event.getUserName() + ",</p>" +
            "<p>Thank you for signing up! Please click the link below to verify your email:</p>" +
            "<p><a href=\"" + event.getVerificationUrl() + "\">Verify Email</a></p>" +
            "<p>Or copy this link: " + event.getVerificationUrl() + "</p>" +
            "<p>This link expires in 24 hours.</p>" +
            "<p>Best regards,<br>Pharmacy System Team</p>" +
            "</body></html>";
    }

    private String buildLoginAlertBody(LoginAlertEvent event) {
        return "<html><body>" +
            "<h2>New Login to Your Account</h2>" +
            "<p>Hello " + event.getUserName() + ",</p>" +
            "<p>We detected a new login to your account:</p>" +
            "<ul>" +
            "<li><strong>Time:</strong> " + event.getLoginTime() + "</li>" +
            "<li><strong>IP Address:</strong> " + event.getIpAddress() + "</li>" +
            "<li><strong>Device:</strong> " + event.getDeviceInfo() + "</li>" +
            "</ul>" +
            "<p>If this was you, no action needed.</p>" +
            "<p>If you didn't log in, please secure your account immediately.</p>" +
            "<p>Best regards,<br>Pharmacy System Security Team</p>" +
            "</body></html>";
    }

    private String buildOtpEmailBody(OtpDeliveryEvent event) {
        return "<html><body>" +
            "<h2>Your Verification Code</h2>" +
            "<p>Hello " + event.getUserName() + ",</p>" +
            "<p>Your one-time password (OTP) is:</p>" +
            "<h1 style='font-size: 32px; letter-spacing: 5px;'>" + event.getOtpCode() + "</h1>" +
            "<p>This code expires in " + event.getExpirationMinutes() + " minutes.</p>" +
            "<p>Do not share this code with anyone.</p>" +
            "<p>Best regards,<br>Pharmacy System Team</p>" +
            "</body></html>";
    }

    private String buildPasswordResetBody(PasswordResetEvent event) {
        return "<html><body>" +
            "<h2>Reset Your Password</h2>" +
            "<p>Hello " + event.getUserName() + ",</p>" +
            "<p>We received a request to reset your password. Click the link below to create a new password:</p>" +
            "<p><a href=\"" + event.getResetUrl() + "\">Reset Password</a></p>" +
            "<p>Or copy this link: " + event.getResetUrl() + "</p>" +
            "<p>This link expires in 1 hour.</p>" +
            "<p>If you didn't request a password reset, please ignore this email and your password will remain unchanged.</p>" +
            "<p>Best regards,<br>Pharmacy System Team</p>" +
            "</body></html>";
    }
}

package org.sprint.authService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.expiration-minutes:10}")
    private int expirationMinutes;

    @Value("${otp.max-attempts:3}")
    private int maxAttempts;

    @Value("${otp.length:6}")
    private int otpLength;

    private static final String OTP_PREFIX = "otp:";
    private static final String RATE_LIMIT_PREFIX = "otp:ratelimit:";

    public String generateOtp(String identifier) {
        String otp = generateRandomOtp();
        Map<String, String> otpData = new HashMap<>();
        otpData.put("code", otp);
        otpData.put("attempts", "0");
        String key = OTP_PREFIX + identifier;
        redisTemplate.opsForHash().putAll(key, otpData);
        redisTemplate.expire(key, Duration.ofMinutes(expirationMinutes));
        return otp;
    }

    public boolean validateOtp(String identifier, String otp) {
        String key = OTP_PREFIX + identifier;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return false;
        }
        String storedOtp = (String) redisTemplate.opsForHash().get(key, "code");
        String attemptsStr = (String) redisTemplate.opsForHash().get(key, "attempts");
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        if (attempts >= maxAttempts) {
            invalidateOtp(identifier);
            return false;
        }
        if (storedOtp != null && storedOtp.equals(otp)) {
            return true;
        }
        redisTemplate.opsForHash().put(key, "attempts", String.valueOf(attempts + 1));
        return false;
    }

    public boolean isRateLimited(String identifier) {
        return false;
    }

    public void invalidateOtp(String identifier) {
        String key = OTP_PREFIX + identifier;
        redisTemplate.delete(key);
    }

    private String generateRandomOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }
}

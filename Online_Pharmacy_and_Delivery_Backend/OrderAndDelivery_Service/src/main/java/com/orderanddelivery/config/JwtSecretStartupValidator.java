package com.orderanddelivery.config;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtSecretStartupValidator implements ApplicationRunner {

    private static final String INSECURE_DEFAULT_SECRET = "ThisIsADevOnlyJwtSecretKeyForHS256SigningAtLeast32BytesLong123456";
    private static final Set<String> NON_STRICT_PROFILES = Set.of("dev", "local", "test");

    private final Environment environment;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Override
    public void run(ApplicationArguments args) {
        if (isNonStrictProfileActive()) {
            return;
        }

        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("JWT secret is not configured. Set JWT_SECRET in non-dev environments.");
        }

        String normalizedSecret = jwtSecret.trim();
        if (INSECURE_DEFAULT_SECRET.equals(normalizedSecret)) {
            throw new IllegalStateException(
                    "Insecure default JWT secret detected for non-dev profile. Configure JWT_SECRET before startup.");
        }

        if (normalizedSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256.");
        }
    }

    private boolean isNonStrictProfileActive() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if (NON_STRICT_PROFILES.contains(profile.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}

package org.sprint.authService.messaging;

public final class AuthEventRoutingKeys {
    public static final String EMAIL_VERIFICATION = "auth.email.verification";
    public static final String LOGIN_ALERT = "auth.email.login-alert";
    public static final String OTP_DELIVERY = "auth.otp.delivery";
    public static final String PASSWORD_RESET = "auth.password.reset";
    private AuthEventRoutingKeys() {}
}

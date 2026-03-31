package com.pharmacy.apigateway.swagger;

public enum ApiAccessLevel {
    PUBLIC,
    AUTHENTICATED,
    CUSTOMER,
    ADMIN;

    public boolean isAllowedFor(SwaggerUserContext userContext) {
        return switch (this) {
            case PUBLIC -> true;
            case AUTHENTICATED -> userContext.authenticated();
            case CUSTOMER -> userContext.customer();
            case ADMIN -> userContext.admin();
        };
    }

    public boolean requiresJwt() {
        return this != PUBLIC;
    }
}

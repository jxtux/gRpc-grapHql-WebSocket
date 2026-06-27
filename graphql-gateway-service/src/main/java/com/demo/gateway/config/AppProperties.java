package com.demo.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {
    @Value("${app.services.user-service-url}")
    private String userServiceUrl;
    @Value("${app.services.relation-service-url}")
    private String relationServiceUrl;
    @Value("${app.security.internal-api-key}")
    private String internalApiKey;
    @Value("${app.security.jwt-secret}")
    private String jwtSecret;

    public String getUserServiceUrl() { return userServiceUrl; }
    public String getRelationServiceUrl() { return relationServiceUrl; }
    public String getInternalApiKey() { return internalApiKey; }
    public String getJwtSecret() { return jwtSecret; }
}

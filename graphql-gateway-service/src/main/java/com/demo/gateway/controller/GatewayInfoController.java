package com.demo.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
public class GatewayInfoController {
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "graphql-gateway-service");
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "name", "graphql-gateway-service",
                "description", "API Gateway GraphQL con resolvers hacia usuario-service y relaciones-service",
                "graphql", "/graphql",
                "graphiql", "/graphiql"
        );
    }
}

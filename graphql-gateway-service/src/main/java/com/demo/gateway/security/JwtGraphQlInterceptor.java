package com.demo.gateway.security;

import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class JwtGraphQlInterceptor implements WebGraphQlInterceptor {
    private final JwtService jwtService;

    public JwtGraphQlInterceptor(JwtService jwtService) { this.jwtService = jwtService; }

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Long userId = jwtService.validateAndGetUserId(token);
                request.configureExecutionInput((executionInput, builder) ->
                        builder.graphQLContext(Map.of("authUserId", userId)).build());
            } catch (IllegalArgumentException ignored) {
                request.configureExecutionInput((executionInput, builder) ->
                        builder.graphQLContext(Map.of("authError", "Token inválido")).build());
            }
        }
        return chain.next(request);
    }
}

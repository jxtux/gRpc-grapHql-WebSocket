package com.demo.gateway.client;

import com.demo.gateway.config.AppProperties;
import com.demo.gateway.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class UserServiceClient {
    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);
    private final RestClient restClient;
    private final AppProperties properties;

    public UserServiceClient(AppProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().baseUrl(properties.getUserServiceUrl()).build();
    }

    public LoginResponse login(String username, String password) {
        log.info("Gateway llamando usuario-service /login para {}", username);
        return restClient.post()
                .uri("/api/users/login")
                .header("X-Internal-Api-Key", properties.getInternalApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(username, password))
                .retrieve()
                .body(LoginResponse.class);
    }

    public UserDto getById(Long id) {
        log.info("Gateway llamando usuario-service getById {}", id);
        return restClient.get()
                .uri("/api/users/{id}", id)
                .header("X-Internal-Api-Key", properties.getInternalApiKey())
                .retrieve()
                .body(UserDto.class);
    }

    public List<UserDto> batch(List<Long> ids) {
        log.info("Gateway llamando usuario-service /batch con ids {}", ids);
        return restClient.post()
                .uri("/api/users/batch")
                .header("X-Internal-Api-Key", properties.getInternalApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UserBatchRequest(ids))
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserDto>>() {});
    }
}

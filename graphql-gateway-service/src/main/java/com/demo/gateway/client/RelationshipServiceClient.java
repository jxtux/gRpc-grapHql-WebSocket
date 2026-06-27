package com.demo.gateway.client;

import com.demo.gateway.config.AppProperties;
import com.demo.gateway.dto.RelationshipDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class RelationshipServiceClient {
    private static final Logger log = LoggerFactory.getLogger(RelationshipServiceClient.class);
    private final RestClient restClient;
    private final AppProperties properties;

    public RelationshipServiceClient(AppProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().baseUrl(properties.getRelationServiceUrl()).build();
    }

    public List<RelationshipDto> getByUser(Long userId) {
        log.info("Gateway llamando relaciones-service para userId={}", userId);
        return restClient.get()
                .uri("/api/relationships/user/{userId}", userId)
                .header("X-Internal-Api-Key", properties.getInternalApiKey())
                .retrieve()
                .body(new ParameterizedTypeReference<List<RelationshipDto>>() {});
    }
}

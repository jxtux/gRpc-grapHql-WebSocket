package com.demo.gateway.graphql;

import com.demo.gateway.client.RelationshipServiceClient;
import com.demo.gateway.client.UserServiceClient;
import com.demo.gateway.dto.*;
import com.demo.gateway.security.JwtService;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class GraphQLResolver {
    private static final Logger log = LoggerFactory.getLogger(GraphQLResolver.class);
    private final UserServiceClient userClient;
    private final RelationshipServiceClient relationshipClient;
    private final JwtService jwtService;

    public GraphQLResolver(UserServiceClient userClient, RelationshipServiceClient relationshipClient, JwtService jwtService) {
        this.userClient = userClient;
        this.relationshipClient = relationshipClient;
        this.jwtService = jwtService;
    }

    @QueryMapping
    public AuthPayload login(@Argument String username, @Argument String password) {
        log.info("Resolver login ejecutado para username={}", username);
        LoginResponse response = userClient.login(username, password);
        if (response != null && response.authenticated() && response.user() != null) {
            String token = jwtService.generateToken(response.user().id(), response.user().username());
            return new AuthPayload(true, token, response.user(), "Login correcto");
        }
        return new AuthPayload(false, null, null, response == null ? "Error autenticando" : response.message());
    }

    @QueryMapping
    public UserDto me(@Argument Long userId, DataFetchingEnvironment env) {
        requireSameUser(userId, env);
        log.info("Resolver me ejecutado para userId={}", userId);
        return userClient.getById(userId);
    }

    @QueryMapping
    public List<UserRelationDetail> userRelations(@Argument Long userId, DataFetchingEnvironment env) {
        requireSameUser(userId, env);
        log.info("Resolver userRelations ejecutado para userId={}", userId);
        List<RelationshipDto> relations = Optional.ofNullable(relationshipClient.getByUser(userId)).orElse(List.of());
        List<Long> ids = relations.stream().map(RelationshipDto::targetUserId).distinct().toList();
        if (ids.isEmpty()) return List.of();
        Map<Long, UserDto> usersById = userClient.batch(ids).stream().collect(Collectors.toMap(UserDto::id, Function.identity()));
        return relations.stream()
                .map(r -> new UserRelationDetail(usersById.get(r.targetUserId()), r.type(), r.status()))
                .filter(d -> d.user() != null)
                .toList();
    }

    private void requireSameUser(Long requestedUserId, DataFetchingEnvironment env) {
        Long authUserId = env.getGraphQlContext().get("authUserId");
        if (authUserId == null) {
            throw new SecurityException("Token JWT requerido o inválido");
        }
        if (!authUserId.equals(requestedUserId)) {
            throw new SecurityException("No puedes consultar información de otro usuario");
        }
    }
}

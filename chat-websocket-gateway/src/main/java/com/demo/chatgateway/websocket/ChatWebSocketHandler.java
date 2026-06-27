package com.demo.chatgateway.websocket;

import com.demo.chat.proto.*;
import com.demo.chatgateway.dto.ChatClientMessage;
import com.demo.chatgateway.dto.ChatOutboundMessage;
import com.demo.chatgateway.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final JwtUtil jwtUtil;
    private final String grpcHost;
    private final int grpcPort;
    private final Map<String, GrpcSessionContext> contexts = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(JwtUtil jwtUtil,
                                @Value("${chat.grpc.host}") String grpcHost,
                                @Value("${chat.grpc.port}") int grpcPort) {
        this.jwtUtil = jwtUtil;
        this.grpcHost = grpcHost;
        this.grpcPort = grpcPort;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, String> params = queryParams(session.getUri());
        Long userId = Long.valueOf(params.getOrDefault("userId", "0"));
        String token = params.getOrDefault("token", "");
        Long tokenUserId = jwtUtil.validateAndGetUserId(token);
        if (!tokenUserId.equals(userId)) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("JWT no corresponde al usuario"));
            return;
        }

        ManagedChannel channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort).usePlaintext().build();
        ChatServiceGrpc.ChatServiceStub asyncStub = ChatServiceGrpc.newStub(channel);
        ChatServiceGrpc.ChatServiceBlockingStub blockingStub = ChatServiceGrpc.newBlockingStub(channel);

        StreamObserver<ChatServerEvent> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(ChatServerEvent event) {
                send(session, toOutbound(event));
            }
            @Override
            public void onError(Throwable throwable) {
                log.warn("Error en stream gRPC hacia websocket {}: {}", session.getId(), throwable.getMessage());
                sendError(session, throwable.getMessage());
            }
            @Override
            public void onCompleted() {
                log.info("Stream gRPC completado para websocket {}", session.getId());
            }
        };

        StreamObserver<ChatClientEvent> requestObserver = asyncStub.chatStream(responseObserver);
        contexts.put(session.getId(), new GrpcSessionContext(userId, channel, requestObserver, blockingStub));
        requestObserver.onNext(ChatClientEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setFromUserId(userId)
                .setType(ChatEventType.CONNECT)
                .setToken(token)
                .build());
        log.info("WebSocket conectado userId={} sessionId={}", userId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        GrpcSessionContext ctx = contexts.get(session.getId());
        if (ctx == null) return;
        ChatClientMessage inbound = mapper.readValue(message.getPayload(), ChatClientMessage.class);
        String type = inbound.type() == null ? "MESSAGE" : inbound.type().toUpperCase();
        if ("HISTORY".equals(type)) {
            ChatHistoryResponse response = ctx.blockingStub.getHistory(ChatHistoryRequest.newBuilder()
                    .setUserId(ctx.userId)
                    .setOtherUserId(inbound.toUserId())
                    .build());
            for (ChatServerEvent event : response.getMessagesList()) send(session, toOutbound(event));
            return;
        }
        if ("PRESENCE".equals(type)) {
            UserPresenceResponse presence = ctx.blockingStub.getPresence(UserPresenceRequest.newBuilder()
                    .setUserId(inbound.toUserId())
                    .build());
            send(session, new ChatOutboundMessage(UUID.randomUUID().toString(), "PRESENCE", presence.getUserId(), ctx.userId, "", presence.getLastSeenAt(), presence.getOnline()));
            return;
        }
        ctx.requestObserver.onNext(ChatClientEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setFromUserId(ctx.userId)
                .setToUserId(inbound.toUserId())
                .setContent(inbound.content() == null ? "" : inbound.content())
                .setType(ChatEventType.MESSAGE)
                .build());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        GrpcSessionContext ctx = contexts.remove(session.getId());
        if (ctx != null) {
            try {
                ctx.requestObserver.onNext(ChatClientEvent.newBuilder()
                        .setEventId(UUID.randomUUID().toString())
                        .setFromUserId(ctx.userId)
                        .setType(ChatEventType.DISCONNECT)
                        .build());
                ctx.requestObserver.onCompleted();
            } catch (Exception ignored) {}
            ctx.channel.shutdownNow();
            log.info("WebSocket cerrado userId={} sessionId={}", ctx.userId, session.getId());
        }
    }

    private Map<String, String> queryParams(URI uri) {
        return UriComponentsBuilder.fromUri(uri).build().getQueryParams().toSingleValueMap();
    }

    private void send(WebSocketSession session, ChatOutboundMessage outbound) {
        try {
            if (session.isOpen()) session.sendMessage(new TextMessage(mapper.writeValueAsString(outbound)));
        } catch (Exception ex) {
            log.warn("No se pudo enviar mensaje WebSocket", ex);
        }
    }

    private void sendError(WebSocketSession session, String error) {
        send(session, new ChatOutboundMessage(UUID.randomUUID().toString(), "ERROR", 0L, 0L, error, Instant.now().toString(), false));
    }

    private ChatOutboundMessage toOutbound(ChatServerEvent event) {
        return new ChatOutboundMessage(event.getEventId(), event.getType().name(), event.getFromUserId(), event.getToUserId(), event.getContent(), event.getCreatedAt(), event.getOnline());
    }

    private record GrpcSessionContext(Long userId, ManagedChannel channel,
                                      StreamObserver<ChatClientEvent> requestObserver,
                                      ChatServiceGrpc.ChatServiceBlockingStub blockingStub) {}
}

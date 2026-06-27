package com.demo.chatgateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat-gateway")
public class ChatGatewayInfoController {
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "chat-websocket-gateway");
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "name", "chat-websocket-gateway",
                "websocket", "ws://localhost:8086/ws/chat?userId=1&token=JWT",
                "description", "Gateway WebSocket que consume chat-grpc-service con gRPC bidirectional streaming"
        );
    }
}

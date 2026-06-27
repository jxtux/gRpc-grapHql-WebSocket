package com.demo.chatgateway.dto;

public record ChatOutboundMessage(String eventId, String type, Long fromUserId, Long toUserId,
                                  String content, String createdAt, boolean online) {}

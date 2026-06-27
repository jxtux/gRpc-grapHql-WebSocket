package com.demo.chatgateway.dto;

public record ChatClientMessage(String type, Long fromUserId, Long toUserId, String content) {}

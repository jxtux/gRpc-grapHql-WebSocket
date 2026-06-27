package com.demo.gateway.dto;

public record AuthPayload(boolean authenticated, String token, UserDto user, String message) {}

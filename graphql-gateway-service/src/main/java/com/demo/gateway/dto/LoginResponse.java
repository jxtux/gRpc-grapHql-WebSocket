package com.demo.gateway.dto;

public record LoginResponse(boolean authenticated, String message, UserDto user) {}

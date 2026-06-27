package com.demo.usuario.dto;

public record LoginResponse(boolean authenticated, String message, UserDto user) {}

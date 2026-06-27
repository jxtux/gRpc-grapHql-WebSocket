package com.demo.usuario.dto;

public record UserDto(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phone,
        String profession,
        String description,
        String avatarUrl
) {}

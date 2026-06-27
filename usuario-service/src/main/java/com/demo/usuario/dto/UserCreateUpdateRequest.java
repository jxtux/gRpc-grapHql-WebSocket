package com.demo.usuario.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreateUpdateRequest(
        @NotBlank String username,
        String password,
        String firstName,
        String lastName,
        String email,
        String phone,
        String profession,
        String description,
        String avatarUrl
) {}

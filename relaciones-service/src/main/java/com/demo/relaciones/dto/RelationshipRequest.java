package com.demo.relaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RelationshipRequest(
        @NotNull Long sourceUserId,
        @NotNull Long targetUserId,
        @NotBlank String type,
        @NotBlank String status
) {}

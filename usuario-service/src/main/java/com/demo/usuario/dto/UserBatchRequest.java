package com.demo.usuario.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UserBatchRequest(@NotEmpty List<Long> ids) {}

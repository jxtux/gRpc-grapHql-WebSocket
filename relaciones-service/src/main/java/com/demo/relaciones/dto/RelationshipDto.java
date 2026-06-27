package com.demo.relaciones.dto;

public record RelationshipDto(Long id, Long sourceUserId, Long targetUserId, String type, String status) {}

package com.demo.gateway.dto;

public record RelationshipDto(Long id, Long sourceUserId, Long targetUserId, String type, String status) {}

package com.demo.relaciones.controller;

import com.demo.relaciones.dto.*;
import com.demo.relaciones.service.RelationshipService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/relationships")
public class RelationshipController {
    private final RelationshipService relationshipService;
    public RelationshipController(RelationshipService relationshipService) { this.relationshipService = relationshipService; }

    @GetMapping("/user/{userId}")
    public List<RelationshipDto> getByUser(@PathVariable Long userId) { return relationshipService.getByUser(userId); }

    @GetMapping("/user/{userId}/friends")
    public List<RelationshipDto> getFriends(@PathVariable Long userId) { return relationshipService.getFriends(userId); }

    @GetMapping("/user/{userId}/followers")
    public List<RelationshipDto> getFollowers(@PathVariable Long userId) { return relationshipService.getFollowers(userId); }

    @PostMapping
    public RelationshipDto create(@Valid @RequestBody RelationshipRequest request) { return relationshipService.create(request); }

    @PutMapping("/{id}/status")
    public RelationshipDto updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return relationshipService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        relationshipService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.demo.relaciones.service;

import com.demo.relaciones.dto.*;
import com.demo.relaciones.entity.*;
import com.demo.relaciones.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RelationshipService {
    private static final Logger log = LoggerFactory.getLogger(RelationshipService.class);
    private final UserRelationshipRepository relationshipRepository;
    private final RelationshipStatusRepository statusRepository;

    public RelationshipService(UserRelationshipRepository relationshipRepository, RelationshipStatusRepository statusRepository) {
        this.relationshipRepository = relationshipRepository;
        this.statusRepository = statusRepository;
    }

    @Transactional(readOnly = true)
    public List<RelationshipDto> getByUser(Long userId) {
        log.info("Consultando relaciones del usuario {}", userId);
        return relationshipRepository.findBySourceUserId(userId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<RelationshipDto> getFriends(Long userId) {
        return relationshipRepository.findBySourceUserIdAndRelationshipType(userId, RelationshipType.FRIEND).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<RelationshipDto> getFollowers(Long userId) {
        return relationshipRepository.findBySourceUserIdAndRelationshipType(userId, RelationshipType.FOLLOWER).stream().map(this::toDto).toList();
    }

    @Transactional
    public RelationshipDto create(RelationshipRequest request) {
        var status = statusRepository.findByCode(request.status()).orElseThrow();
        UserRelationshipEntity r = new UserRelationshipEntity();
        r.setSourceUserId(request.sourceUserId());
        r.setTargetUserId(request.targetUserId());
        r.setRelationshipType(RelationshipType.valueOf(request.type()));
        r.setStatus(status);
        return toDto(relationshipRepository.save(r));
    }

    @Transactional
    public RelationshipDto updateStatus(Long id, StatusUpdateRequest request) {
        var relation = relationshipRepository.findById(id).orElseThrow();
        var status = statusRepository.findByCode(request.status()).orElseThrow();
        relation.setStatus(status);
        return toDto(relationshipRepository.save(relation));
    }

    @Transactional
    public void delete(Long id) { relationshipRepository.deleteById(id); }

    private RelationshipDto toDto(UserRelationshipEntity r) {
        return new RelationshipDto(r.getId(), r.getSourceUserId(), r.getTargetUserId(), r.getRelationshipType().name(), r.getStatus().getCode());
    }
}

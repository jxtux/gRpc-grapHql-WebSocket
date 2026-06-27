package com.demo.relaciones.repository;

import com.demo.relaciones.entity.RelationshipType;
import com.demo.relaciones.entity.UserRelationshipEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRelationshipRepository extends JpaRepository<UserRelationshipEntity, Long> {
    @EntityGraph(attributePaths = "status")
    List<UserRelationshipEntity> findBySourceUserId(Long sourceUserId);

    @EntityGraph(attributePaths = "status")
    List<UserRelationshipEntity> findBySourceUserIdAndRelationshipType(Long sourceUserId, RelationshipType type);
}

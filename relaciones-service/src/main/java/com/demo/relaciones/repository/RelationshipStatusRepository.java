package com.demo.relaciones.repository;

import com.demo.relaciones.entity.RelationshipStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RelationshipStatusRepository extends JpaRepository<RelationshipStatusEntity, Long> {
    Optional<RelationshipStatusEntity> findByCode(String code);
}

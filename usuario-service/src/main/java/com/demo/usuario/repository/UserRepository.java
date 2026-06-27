package com.demo.usuario.repository;

import com.demo.usuario.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @EntityGraph(attributePaths = "profile")
    Optional<UserEntity> findByUsername(String username);

    @EntityGraph(attributePaths = "profile")
    List<UserEntity> findByIdIn(List<Long> ids);

    @Override
    @EntityGraph(attributePaths = "profile")
    Optional<UserEntity> findById(Long id);
}

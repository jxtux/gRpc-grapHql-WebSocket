package com.demo.chat.repository;

import com.demo.chat.entity.UserPresenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPresenceRepository extends JpaRepository<UserPresenceEntity, Long> {}

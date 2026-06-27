package com.demo.chat.repository;

import com.demo.chat.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    @Query("""
        select m from ChatMessageEntity m
        where (m.fromUserId = :userId and m.toUserId = :otherUserId)
           or (m.fromUserId = :otherUserId and m.toUserId = :userId)
        order by m.sentAt asc
    """)
    List<ChatMessageEntity> findConversation(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);
}

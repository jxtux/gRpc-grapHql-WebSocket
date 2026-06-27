package com.demo.chat.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_presence")
public class UserPresenceEntity {
    @Id
    private Long userId;
    @Column(nullable = false)
    private boolean online;
    private Instant lastSeenAt;
    private Instant updatedAt;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
    public Instant getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Instant lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

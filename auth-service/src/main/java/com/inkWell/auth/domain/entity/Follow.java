package com.inkWell.auth.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "follows", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"follower_id", "followed_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    @Column(name = "followed_id", nullable = false)
    private Long followedId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFollowerId() { return followerId; }
    public void setFollowerId(Long followerId) { this.followerId = followerId; }
    public Long getFollowedId() { return followedId; }
    public void setFollowedId(Long followedId) { this.followedId = followedId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public static FollowBuilder builder() {
        return new FollowBuilder();
    }

    public static class FollowBuilder {
        private Follow follow = new Follow();
        public FollowBuilder id(Long id) { follow.id = id; return this; }
        public FollowBuilder followerId(Long followerId) { follow.followerId = followerId; return this; }
        public FollowBuilder followedId(Long followedId) { follow.followedId = followedId; return this; }
        public FollowBuilder status(String status) { follow.status = status; return this; }
        public Follow build() { return follow; }
    }
}

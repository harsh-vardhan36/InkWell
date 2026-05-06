package com.inkWell.auth.repository;

import com.inkWell.auth.domain.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);
    long countByFollowedId(Long followedId);
    long countByFollowerId(Long followerId);
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);
    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);
    java.util.List<Follow> findAllByFollowedId(Long followedId);
}

package com.inkWell.auth.repository;

import com.inkWell.auth.domain.entity.User;
import com.inkWell.auth.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByUserId(Long userId);
    
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    
    Optional<User> findByResetToken(String resetToken);
    
    List<User> findAllByRole(Role role);
    
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username%")
    List<User> searchByUsername(String username);
    
    void deleteByUserId(Long userId);
}

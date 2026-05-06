package com.inkWell.auth.domain.entity;

import com.inkWell.auth.domain.enums.Plan;
import com.inkWell.auth.domain.enums.Provider;
import com.inkWell.auth.domain.enums.Role;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a system user.
 * Stores authentication details, profile information, and subscription plans.
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long userId;

    /**
     * Unique username for the user.
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * Unique email address for the user.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * BCrypt hashed password.
     */
    @Column(nullable = false)
    private String passwordHash;

    /**
     * Full name of the user.
     */
    @Column(nullable = false)
    private String fullName;

    /**
     * Role of the user in the system (e.g., ADMIN, USER, AUTHOR).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    /**
     * Brief biography of the user.
     */
    private String bio;
    
    /**
     * URL to the user's profile picture.
     */
    private String avatarUrl;

    /**
     * User's contact phone number.
     */
    private String contactNumber;

    /**
     * Current subscription plan (FREE, PRO).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Plan plan = Plan.FREE;

    /**
     * Expiration timestamp for the current plan.
     */
    private LocalDateTime planExpiry;

    /**
     * Authentication provider (LOCAL, GOOGLE, GITHUB).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    /**
     * Whether the user account is currently active.
     */
    @Column(nullable = false)
    private boolean isActive;

    /**
     * Token used for password reset flows.
     */
    private String resetToken;

    /**
     * Expiration timestamp for the reset token.
     */
    private LocalDateTime resetTokenExpiry;

    /**
     * Timestamp when the user record was created.
     */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user record was last updated.
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Manual Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public LocalDateTime getPlanExpiry() { return planExpiry; }
    public void setPlanExpiry(LocalDateTime planExpiry) { this.planExpiry = planExpiry; }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private String username;
        private String email;
        private String passwordHash;
        private String fullName;
        private Role role;
        private Provider provider;
        private boolean isActive;
        private Plan plan = Plan.FREE;

        public UserBuilder username(String username) { this.username = username; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public UserBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserBuilder role(Role role) { this.role = role; return this; }
        public UserBuilder provider(Provider provider) { this.provider = provider; return this; }
        public UserBuilder isActive(boolean isActive) { this.isActive = isActive; return this; }
        public UserBuilder plan(Plan plan) { this.plan = plan; return this; }

        public User build() {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(passwordHash);
            user.setFullName(fullName);
            user.setRole(role);
            user.setProvider(provider);
            user.setActive(isActive);
            user.setPlan(plan);
            return user;
        }
    }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
}

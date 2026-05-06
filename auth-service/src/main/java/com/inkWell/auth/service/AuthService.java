package com.inkWell.auth.service;

import com.inkWell.auth.domain.entity.User;
import com.inkWell.auth.dto.*;

import java.util.List;

/**
 * Service interface for authentication and user-related operations.
 * Handles registration, login, token management, profile updates, 
 * social features (follow/unfollow), and security flows like password resets.
 */
public interface AuthService {
    /**
     * Registers a new user in the system.
     * 
     * @param request The registration details.
     * @return An {@link AuthResponse} containing JWT tokens and user info.
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user with email and password.
     * 
     * @param request The login credentials.
     * @return An {@link AuthResponse} containing JWT tokens.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Invalidates a user's session and token.
     * 
     * @param token The token to logout.
     */
    void logout(String token);

    /**
     * Validates if a JWT token is still active and valid.
     * 
     * @param token The token to validate.
     * @throws RuntimeException if the token is invalid.
     */
    void validateToken(String token);

    /**
     * Refreshes an expired access token using a valid refresh token.
     * 
     * @param refreshToken The refresh token.
     * @return A new {@link AuthResponse} with a fresh access token.
     */
    AuthResponse refreshToken(String refreshToken);
    
    /**
     * Retrieves a user entity by their email address.
     * 
     * @param email The email to search for.
     * @return The {@link User} object.
     */
    User getUserByEmail(String email);

    /**
     * Retrieves a user entity by their unique ID.
     * 
     * @param userId The ID of the user.
     * @return The {@link User} object.
     */
    User getUserById(Long userId);
    
    /**
     * Updates a user's profile information.
     * 
     * @param userId The ID of the user to update.
     * @param request The new profile data.
     */
    void updateProfile(Long userId, ProfileUpdateRequest request);

    /**
     * Changes a user's password.
     * 
     * @param userId The ID of the user.
     * @param request The current and new password details.
     */
    void changePassword(Long userId, ChangePasswordRequest request);
    
    /**
     * Searches for users by their username.
     * 
     * @param username The username fragment to search for.
     * @return A list of matching {@link User}s.
     */
    List<User> searchUsers(String username);

    /**
     * Initiates a request to deactivate a user account.
     * 
     * @param userId The ID of the user.
     */
    void requestDeactivation(Long userId);

    /**
     * Verifies the deactivation request using an OTP.
     * 
     * @param userId The ID of the user.
     * @param otp The one-time password sent to the user.
     */
    void verifyDeactivation(Long userId, String otp);

    /**
     * Verifies a general OTP for account verification.
     * 
     * @param email The user's email.
     * @param otp The OTP to verify.
     */
    void verifyOTP(String email, String otp);

    /**
     * Resends a verification OTP to the user's email.
     * 
     * @param email The user's email.
     */
    void resendOTP(String email);

    /**
     * Initiates the forgot password flow by sending a reset link.
     * 
     * @param request The forgot password request containing the email.
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Resets the user's password using a valid reset token.
     * 
     * @param request The reset password request.
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * Initiates a request for a regular user to become an author.
     * 
     * @param email The user's email.
     */
    void requestBecomeAuthor(String email);

    /**
     * Verifies the "become author" request using an OTP.
     * 
     * @param email The user's email.
     * @param otp The OTP to verify.
     */
    void verifyBecomeAuthorOtp(String email, String otp);

    /**
     * Allows a user to follow another user.
     * 
     * @param followerId The ID of the user who is following.
     * @param followedId The ID of the user being followed.
     */
    void followUser(Long followerId, Long followedId);

    /**
     * Allows a user to unfollow someone they are currently following.
     * 
     * @param followerId The ID of the user who is unfollowing.
     * @param followedId The ID of the user being unfollowed.
     */
    void unfollowUser(Long followerId, Long followedId);

    /**
     * Checks if a user is currently following another user.
     * 
     * @param followerId The follower's ID.
     * @param followedId The followed's ID.
     * @return True if following, false otherwise.
     */
    boolean isFollowing(Long followerId, Long followedId);

    /**
     * Gets the total count of followers for a user.
     * 
     * @param userId The user's ID.
     * @return The count of followers.
     */
    long getFollowerCount(Long userId);

    /**
     * Gets the total count of users a specific user is following.
     * 
     * @param userId The user's ID.
     * @return The count of users followed.
     */
    long getFollowingCount(Long userId);

    /**
     * Retrieves a list of users who follow a specific user.
     * 
     * @param userId The user's ID.
     * @return A list of follower {@link User}s.
     */
    List<User> getFollowers(Long userId);

    /**
     * Approves a pending follow request.
     * 
     * @param followerId The ID of the follower.
     * @param followedId The ID of the followed user.
     */
    void approveFollow(Long followerId, Long followedId);

    /**
     * Rejects a pending follow request.
     * 
     * @param followerId The ID of the follower.
     * @param followedId The ID of the followed user.
     */
    void rejectFollow(Long followerId, Long followedId);

    /**
     * Gets the current follow status between two users.
     * 
     * @param followerId The follower's ID.
     * @param followedId The followed's ID.
     * @return A string representing the status (e.g., "FOLLOWING", "PENDING", "NONE").
     */
    String getFollowStatus(Long followerId, Long followedId);
}

package com.inkWell.auth.service;

import com.inkWell.auth.domain.entity.User;
import com.inkWell.auth.dto.*;

import java.util.List;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void logout(String token);
    void validateToken(String token);
    AuthResponse refreshToken(String refreshToken);
    
    User getUserByEmail(String email);
    User getUserById(Long userId);
    
    void updateProfile(Long userId, ProfileUpdateRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    
    List<User> searchUsers(String username);
    void requestDeactivation(Long userId);
    void verifyDeactivation(Long userId, String otp);

    void verifyOTP(String email, String otp);
    void resendOTP(String email);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);

    void requestBecomeAuthor(String email);
    void verifyBecomeAuthorOtp(String email, String otp);
}

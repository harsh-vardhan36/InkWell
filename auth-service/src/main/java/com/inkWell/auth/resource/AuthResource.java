package com.inkWell.auth.resource;

import com.inkWell.auth.domain.entity.User;
import com.inkWell.auth.dto.*;
import com.inkWell.auth.security.JwtUtil;
import com.inkWell.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.inkWell.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String otp,
            @RequestBody(required = false) VerifyRequest body) {
        
        String finalEmail = email;
        String finalOtp = otp;
        if (body != null) {
            if (finalEmail == null) finalEmail = body.getEmail();
            if (finalOtp == null) finalOtp = body.getOtp();
        }
        
        if (finalEmail == null || finalOtp == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and OTP are required"));
        }
        
        authService.verifyOTP(finalEmail, finalOtp);
        return ResponseEntity.ok(Map.of("message", "Account verified successfully. You can now login."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(
            @RequestParam(required = false) String email,
            @RequestBody(required = false) OtpRequest body) {
        
        String finalEmail = email;
        if (body != null && body.getEmail() != null) {
            finalEmail = body.getEmail();
        }
        
        if (finalEmail == null || finalEmail.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        
        authService.resendOTP(finalEmail);
        return ResponseEntity.ok(Map.of("message", "A new OTP has been sent to your email."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(HttpServletRequest request) {
        // Extract email from JWT — the gateway already validated it
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(UserProfileResponse.builder()
            .userId(user.getUserId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .bio(user.getBio())
            .avatarUrl(user.getAvatarUrl())
            .contactNumber(user.getContactNumber())
            .role(user.getRole().name())
            .plan(user.getPlan() != null ? user.getPlan().name() : "FREE")
            .planExpiry(user.getPlanExpiry() != null ? user.getPlanExpiry().toString() : null)
            .provider(user.getProvider().name())
            .isActive(user.isActive())
            .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
            .followerCount(authService.getFollowerCount(user.getUserId()))
            .followingCount(authService.getFollowingCount(user.getUserId()))
            .build());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(UserProfileResponse.builder()
            .userId(user.getUserId())
            .username(user.getUsername())
            .fullName(user.getFullName())
            .bio(user.getBio())
            .avatarUrl(user.getAvatarUrl())
            .role(user.getRole().name())
            .isActive(user.isActive())
            .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
            .followerCount(authService.getFollowerCount(user.getUserId()))
            .followingCount(authService.getFollowingCount(user.getUserId()))
            .build());
    }

    @GetMapping("/followers")
    public ResponseEntity<List<UserProfileResponse>> getFollowers(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        List<User> followers = authService.getFollowers(user.getUserId());
        List<UserProfileResponse> response = followers.stream()
            .map(u -> {
                String status = "PENDING";
                try {
                    status = authService.getFollowStatus(u.getUserId(), user.getUserId());
                } catch (Exception e) {}
                
                return UserProfileResponse.builder()
                    .userId(u.getUserId())
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .fullName(u.getFullName())
                    .avatarUrl(u.getAvatarUrl())
                    .bio(u.getBio())
                    .createdAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null)
                    .isActive(u.isActive())
                    .role(u.getRole().name())
                    .followStatus(status)
                    .build();
            })
            .toList();
        return ResponseEntity.ok(response);
    }

@PostMapping("/follow/{userId}")
public ResponseEntity<Map<String, String>> followUser(HttpServletRequest request, @PathVariable Long userId) {
    User follower = getAuthenticatedUser(request);
    authService.followUser(follower.getUserId(), userId);
    return ResponseEntity.ok(Map.of("message", "Followed successfully"));
}

@PostMapping("/unfollow/{userId}")
public ResponseEntity<Map<String, String>> unfollowUser(HttpServletRequest request, @PathVariable Long userId) {
    User follower = getAuthenticatedUser(request);
    authService.unfollowUser(follower.getUserId(), userId);
    return ResponseEntity.ok(Map.of("message", "Unfollowed successfully"));
}

@GetMapping("/is-following/{userId}")
public ResponseEntity<Map<String, Boolean>> isFollowing(HttpServletRequest request, @PathVariable Long userId) {
    try {
        User follower = getAuthenticatedUser(request);
        boolean following = authService.isFollowing(follower.getUserId(), userId);
        return ResponseEntity.ok(Map.of("following", following));
    } catch (Exception e) {
        return ResponseEntity.ok(Map.of("following", false));
    }
}
    @PostMapping("/follow/{followerId}/approve")
    public ResponseEntity<Map<String, String>> approveFollow(HttpServletRequest request, @PathVariable Long followerId) {
        User author = getAuthenticatedUser(request);
        authService.approveFollow(followerId, author.getUserId());
        return ResponseEntity.ok(Map.of("message", "Follow request approved"));
    }

    @PostMapping("/follow/{followerId}/reject")
    public ResponseEntity<Map<String, String>> rejectFollow(HttpServletRequest request, @PathVariable Long followerId) {
        User author = getAuthenticatedUser(request);
        authService.rejectFollow(followerId, author.getUserId());
        return ResponseEntity.ok(Map.of("message", "Follow request rejected"));
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(HttpServletRequest request,
            @Valid @RequestBody ProfileUpdateRequest requestBody) {
        User user = getAuthenticatedUser(request);
        authService.updateProfile(user.getUserId(), requestBody);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(HttpServletRequest request,
            @Valid @RequestBody ChangePasswordRequest passwordRequest) {
        User user = getAuthenticatedUser(request);
        authService.changePassword(user.getUserId(), passwordRequest);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponse>> searchUsers(@RequestParam String username) {
        List<User> users = authService.searchUsers(username);
        List<UserProfileResponse> response = users.stream()
            .map(u -> UserProfileResponse.builder()
                .userId(u.getUserId())
                .username(u.getUsername())
                .fullName(u.getFullName())
                .avatarUrl(u.getAvatarUrl())
                .bio(u.getBio())
                .isActive(u.isActive())
                .role(u.getRole().name())
                .build())
            .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deactivate/request")
    public ResponseEntity<Map<String, String>> requestDeactivate(HttpServletRequest request) {
        try {
            User user = getAuthenticatedUser(request);
            authService.requestDeactivation(user.getUserId());
            return ResponseEntity.ok(Map.of("message", "OTP sent to your email for account deactivation."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to request deactivation: " + e.getMessage()));
        }
    }

    @PostMapping("/deactivate/verify")
    public ResponseEntity<Map<String, String>> verifyDeactivate(HttpServletRequest request,
                                                                @RequestParam(required = false) String otp,
                                                                @RequestBody(required = false) Map<String, String> body) {
        try {
            String finalOtp = otp;
            if (finalOtp == null && body != null) {
                finalOtp = body.get("otp");
            }
            
            if (finalOtp == null || finalOtp.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "OTP is required."));
            }

            User user = getAuthenticatedUser(request);
            authService.verifyDeactivation(user.getUserId(), finalOtp);
            return ResponseEntity.ok(Map.of("message", "Account deactivated and deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Verification failed: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "Reset token sent to email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PostMapping("/become-author/request")
    public ResponseEntity<Map<String, String>> becomeAuthorRequest(
            @RequestParam(required = false) String email,
            @RequestBody(required = false) OtpRequest body) {
        
        String finalEmail = email;
        if (body != null && body.getEmail() != null) {
            finalEmail = body.getEmail();
        }
        
        if (finalEmail == null || finalEmail.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        
        authService.requestBecomeAuthor(finalEmail);
        return ResponseEntity.ok(Map.of("message", "OTP sent to your email for author verification."));
    }

    @PostMapping("/become-author/verify")
    public ResponseEntity<Map<String, String>> becomeAuthorVerify(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String otp,
            @RequestBody(required = false) VerifyRequest body) {
        
        String finalEmail = email;
        String finalOtp = otp;
        if (body != null) {
            if (finalEmail == null) finalEmail = body.getEmail();
            if (finalOtp == null) finalOtp = body.getOtp();
        }
        
        if (finalEmail == null || finalOtp == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and OTP are required"));
        }
        
        authService.verifyBecomeAuthorOtp(finalEmail, finalOtp);
        return ResponseEntity.ok(Map.of("message", "Congratulations! You are now an Author."));
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        authService.validateToken(token);
        String email = jwtUtil.extractEmail(token);
        return authService.getUserByEmail(email);
    }
}

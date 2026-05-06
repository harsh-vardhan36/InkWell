package com.inkWell.auth.service.impl;

import com.inkWell.auth.domain.entity.User;
import com.inkWell.auth.domain.enums.Provider;
import com.inkWell.auth.domain.enums.Role;
import com.inkWell.auth.dto.*;
import com.inkWell.auth.repository.UserRepository;
import com.inkWell.auth.repository.FollowRepository;
import com.inkWell.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import com.inkWell.auth.exception.UserAlreadyExistsException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .passwordHash("encodedPassword")
                .role(Role.READER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .build();
        testUser.setUserId(1L);

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void register_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("Registration successful. Please verify your email with the OTP sent.", response.getMessage());
        verify(userRepository).save(any(User.class));
        verify(valueOperations).set(startsWith("otp:"), anyString(), any());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationRequest.class));
    }

    @Test
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("testToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationRequest.class));
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongPassword");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void verifyOTP_Success() {
        when(valueOperations.get(anyString())).thenReturn("123456");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        authService.verifyOTP("test@example.com", "123456");

        assertTrue(testUser.isActive());
        verify(userRepository).save(testUser);
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void refreshToken_Success() {
        when(jwtUtil.isTokenValid(anyString())).thenReturn(true);
        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("newToken");

        AuthResponse response = authService.refreshToken("validRefreshToken");

        assertNotNull(response);
        assertEquals("newToken", response.getToken());
    }

    @Test
    void verifyOTP_InvalidOTP_ThrowsException() {
        when(valueOperations.get(anyString())).thenReturn("654321");

        assertThrows(RuntimeException.class, () -> authService.verifyOTP("test@example.com", "123456"));
    }

    @Test
    void resendOTP_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        testUser.setActive(false);

        authService.resendOTP("test@example.com");

        verify(valueOperations).set(eq("otp:test@example.com"), anyString(), any());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationRequest.class));
    }

    @Test
    void resendOTP_AlreadyActive_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        testUser.setActive(true);

        assertThrows(RuntimeException.class, () -> authService.resendOTP("test@example.com"));
    }

    @Test
    void resendOTP_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.resendOTP("test@example.com"));
    }

    @Test
    void logout_Success() {
        when(jwtUtil.getRemainingExpiration(anyString())).thenReturn(1000L);

        authService.logout("someToken");

        verify(valueOperations).set(startsWith("blacklist:"), eq("true"), any());
    }

    @Test
    void logout_ExpiredToken_DoesNothing() {
        when(jwtUtil.getRemainingExpiration(anyString())).thenReturn(0L);
        authService.logout("expiredToken");
        verify(valueOperations, never()).set(anyString(), anyString(), any());
    }

    @Test
    void forgotPassword_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        authService.forgotPassword(new ForgotPasswordRequest("test@example.com"));

        verify(valueOperations).set(startsWith("reset_otp:"), anyString(), any());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationRequest.class));
    }

    @Test
    void resetPassword_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "newPass");
        when(valueOperations.get(anyString())).thenReturn("123456");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPass");

        authService.resetPassword(request);

        verify(userRepository).save(testUser);
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void followUser_SelfFollow_ThrowsException() {
        assertThrows(RuntimeException.class, () -> authService.followUser(1L, 1L));
    }

    @Test
    void followUser_Success() {
        when(followRepository.existsByFollowerIdAndFollowedId(anyLong(), anyLong())).thenReturn(false);

        authService.followUser(1L, 2L);

        verify(followRepository).save(any());
    }

    @Test
    void approveFollow_Success() {
        com.inkWell.auth.domain.entity.Follow follow = com.inkWell.auth.domain.entity.Follow.builder()
                .followerId(2L)
                .followedId(1L)
                .status("PENDING")
                .build();
        when(followRepository.findByFollowerIdAndFollowedId(anyLong(), anyLong())).thenReturn(Optional.of(follow));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        authService.approveFollow(2L, 1L);

        assertEquals("APPROVED", follow.getStatus());
        verify(followRepository).save(follow);
        verify(restTemplate).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void register_UserAlreadyExists_Active_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));
    }

    @Test
    void register_UserAlreadyExists_Inactive_DeletesAndRegisters() {
        testUser.setActive(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        authService.register(registerRequest);

        verify(userRepository).delete(testUser);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameTaken_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));
    }

    @Test
    void login_AccountNotVerified_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        testUser.setActive(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void validateToken_Blacklisted_ThrowsException() {
        when(redisTemplate.hasKey(startsWith("blacklist:"))).thenReturn(true);
        assertThrows(RuntimeException.class, () -> authService.validateToken("blacklistedToken"));
    }

    @Test
    void updateProfile_Success() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFullName("Updated Name");
        request.setBio("New Bio");
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));

        authService.updateProfile(1L, request);

        assertEquals("Updated Name", testUser.getFullName());
        assertEquals("New Bio", testUser.getBio());
        verify(userRepository).save(testUser);
    }

    @Test
    void changePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest("password123", "newPassword");
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncoded");

        authService.changePassword(1L, request);

        assertEquals("newEncoded", testUser.getPasswordHash());
        verify(userRepository).save(testUser);
    }

    @Test
    void requestDeactivation_Success() {
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));
        authService.requestDeactivation(1L);
        verify(valueOperations).set(startsWith("deactivate_otp:"), anyString(), any());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationRequest.class));
    }

    @Test
    void verifyDeactivation_Success() {
        when(valueOperations.get(startsWith("deactivate_otp:"))).thenReturn("1234");
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));

        authService.verifyDeactivation(1L, "1234");

        verify(userRepository).delete(testUser);
    }

    @Test
    void requestBecomeAuthor_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        authService.requestBecomeAuthor("test@example.com");
        verify(valueOperations).set(startsWith("become_author_otp:"), anyString(), any());
    }

    @Test
    void verifyBecomeAuthorOtp_Success() {
        when(valueOperations.get(startsWith("become_author_otp:"))).thenReturn("1234");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        authService.verifyBecomeAuthorOtp("test@example.com", "1234");

        assertEquals(Role.AUTHOR, testUser.getRole());
        verify(userRepository).save(testUser);
    }

    @Test
    void approveFollow_SyncFailure_LogsError() {
        com.inkWell.auth.domain.entity.Follow follow = com.inkWell.auth.domain.entity.Follow.builder()
                .followerId(2L)
                .followedId(1L)
                .status("PENDING")
                .build();
        when(followRepository.findByFollowerIdAndFollowedId(2L, 1L)).thenReturn(Optional.of(follow));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenThrow(new RuntimeException("Sync failed"));

        // Should not throw exception
        assertDoesNotThrow(() -> authService.approveFollow(2L, 1L));
        assertEquals("APPROVED", follow.getStatus());
    }

    @Test
    void approveFollow_NotFound_ThrowsException() {
        when(followRepository.findByFollowerIdAndFollowedId(anyLong(), anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.approveFollow(1L, 2L));
    }

    @Test
    void approveFollow_UserNotFound_DoesNotSync() {
        com.inkWell.auth.domain.entity.Follow follow = new com.inkWell.auth.domain.entity.Follow();
        when(followRepository.findByFollowerIdAndFollowedId(anyLong(), anyLong())).thenReturn(Optional.of(follow));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        authService.approveFollow(1L, 2L);

        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    void unfollowUser_Success() {
        authService.unfollowUser(1L, 2L);
        verify(followRepository).deleteByFollowerIdAndFollowedId(1L, 2L);
    }

    @Test
    void isFollowing_Success() {
        when(followRepository.existsByFollowerIdAndFollowedId(1L, 2L)).thenReturn(true);
        assertTrue(authService.isFollowing(1L, 2L));
    }

    @Test
    void isFollowing_NullIds_ReturnsFalse() {
        assertFalse(authService.isFollowing(null, 2L));
        assertFalse(authService.isFollowing(1L, null));
    }

    @Test
    void getFollowerCount_Success() {
        when(followRepository.countByFollowedId(1L)).thenReturn(10L);
        assertEquals(10L, authService.getFollowerCount(1L));
    }

    @Test
    void getFollowingCount_Success() {
        when(followRepository.countByFollowerId(1L)).thenReturn(5L);
        assertEquals(5L, authService.getFollowingCount(1L));
    }

    @Test
    void getFollowStatus_Success() {
        com.inkWell.auth.domain.entity.Follow follow = new com.inkWell.auth.domain.entity.Follow();
        follow.setStatus("APPROVED");
        when(followRepository.findByFollowerIdAndFollowedId(1L, 2L)).thenReturn(Optional.of(follow));
        assertEquals("APPROVED", authService.getFollowStatus(1L, 2L));
    }

    @Test
    void getFollowStatus_NotFound_ReturnsNone() {
        when(followRepository.findByFollowerIdAndFollowedId(1L, 2L)).thenReturn(Optional.empty());
        assertEquals("NONE", authService.getFollowStatus(1L, 2L));
    }

    @Test
    void rejectFollow_Success() {
        authService.rejectFollow(1L, 2L);
        verify(followRepository).deleteByFollowerIdAndFollowedId(1L, 2L);
    }

    @Test
    void getFollowers_Success() {
        com.inkWell.auth.domain.entity.Follow follow = com.inkWell.auth.domain.entity.Follow.builder().followerId(2L).build();
        when(followRepository.findAllByFollowedId(1L)).thenReturn(java.util.List.of(follow));
        when(userRepository.findAllById(anyList())).thenReturn(java.util.List.of(new User()));

        assertFalse(authService.getFollowers(1L).isEmpty());
    }

    @Test
    void forgotPassword_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
        assertThrows(RuntimeException.class, () -> authService.forgotPassword(request));
    }

    @Test
    void resetPassword_InvalidOTP_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "wrong", "newPass");
        when(valueOperations.get(anyString())).thenReturn("correct");
        assertThrows(RuntimeException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPassword_UserNotFound_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123", "newPass");
        when(valueOperations.get(anyString())).thenReturn("123");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.resetPassword(request));
    }

    @Test
    void verifyDeactivation_InvalidOTP_ThrowsException() {
        when(valueOperations.get(anyString())).thenReturn("wrong");
        assertThrows(RuntimeException.class, () -> authService.verifyDeactivation(1L, "correct"));
    }

    @Test
    void requestBecomeAuthor_AlreadyAuthor_ThrowsException() {
        testUser.setRole(Role.AUTHOR);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        assertThrows(RuntimeException.class, () -> authService.requestBecomeAuthor("test@example.com"));
    }

    @Test
    void verifyBecomeAuthorOtp_InvalidOTP_ThrowsException() {
        when(valueOperations.get(anyString())).thenReturn("wrong");
        assertThrows(RuntimeException.class, () -> authService.verifyBecomeAuthorOtp("test@example.com", "correct"));
    }

    @Test
    void verifyBecomeAuthorOtp_UserNotFound_ThrowsException() {
        when(valueOperations.get(anyString())).thenReturn("1234");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.verifyBecomeAuthorOtp("test@example.com", "1234"));
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        LoginRequest request = new LoginRequest("none@ex.com", "pw");
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_WrongPassword_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        LoginRequest request = new LoginRequest("test@ex.com", "wrong");
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void validateToken_Success() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(jwtUtil.isTokenValid(anyString())).thenReturn(true);
        assertDoesNotThrow(() -> authService.validateToken("validToken"));
    }

    @Test
    void validateToken_Invalid_ThrowsException() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(jwtUtil.isTokenValid(anyString())).thenReturn(false);
        assertThrows(RuntimeException.class, () -> authService.validateToken("invalidToken"));
    }

    @Test
    void refreshToken_InvalidToken_ThrowsException() {
        when(jwtUtil.isTokenValid(anyString())).thenReturn(false);
        assertThrows(RuntimeException.class, () -> authService.refreshToken("invalid"));
    }

    @Test
    void refreshToken_UserNotFound_ThrowsException() {
        when(jwtUtil.isTokenValid(anyString())).thenReturn(true);
        when(jwtUtil.extractEmail(anyString())).thenReturn("test@ex.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.refreshToken("valid"));
    }

    @Test
    void searchUsers_Success() {
        when(userRepository.searchUsers(anyString(), any(), any())).thenReturn(java.util.List.of(testUser));
        assertFalse(authService.searchUsers("test").isEmpty());
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        assertEquals(testUser, authService.getUserByEmail("test@ex.com"));
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.getUserById(99L));
    }

    @Test
    void updateProfile_PartialUpdates() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setAvatarUrl("http://new-avatar.com");
        // fullName and bio are null
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));

        authService.updateProfile(1L, request);

        assertEquals("Test User", testUser.getFullName()); // Unchanged
        assertEquals("http://new-avatar.com", testUser.getAvatarUrl());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateProfile_AllNullFields() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));

        authService.updateProfile(1L, request);

        assertEquals("Test User", testUser.getFullName());
        verify(userRepository).save(testUser);
    }

    @Test
    void validateToken_RevokedToken_ThrowsException() {
        when(redisTemplate.hasKey("blacklist:revokedToken")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> authService.validateToken("revokedToken"));
    }

    @Test
    void followUser_AlreadyFollowing_DoesNothing() {
        when(followRepository.existsByFollowerIdAndFollowedId(1L, 2L)).thenReturn(true);
        authService.followUser(1L, 2L);
        verify(followRepository, never()).save(any());
    }

    @Test
    void isFollowing_EitherIdNull_ReturnsFalse() {
        assertFalse(authService.isFollowing(null, 1L));
        assertFalse(authService.isFollowing(1L, null));
    }
}

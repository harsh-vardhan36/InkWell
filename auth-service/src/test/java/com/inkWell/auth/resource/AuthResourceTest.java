package com.inkWell.auth.resource;

import com.inkWell.auth.domain.entity.User;
import com.inkWell.auth.domain.enums.Role;
import com.inkWell.auth.dto.*;
import com.inkWell.auth.repository.UserRepository;
import com.inkWell.auth.security.JwtUtil;
import com.inkWell.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthResource.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("Password123!");
        request.setFullName("New User");

        AuthResponse authResponse = AuthResponse.builder()
                .message("Registration successful. Please verify your email with the OTP sent.")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful. Please verify your email with the OTP sent."));
    }

    @Test
    void shouldVerifyOtpSuccessfully() throws Exception {
        VerifyRequest verifyRequest = new VerifyRequest("test@example.com", "123456");

        mockMvc.perform(post("/auth/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account verified successfully. You can now login."));
    }

    @Test
    void shouldResendOtpSuccessfully() throws Exception {
        OtpRequest otpRequest = new OtpRequest("test@example.com");

        mockMvc.perform(post("/auth/resend-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("A new OTP has been sent to your email."));
    }

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer some-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest("refresh-token");
        AuthResponse authResponse = AuthResponse.builder().token("new-access-token").build();

        when(authService.refreshToken(anyString())).thenReturn(authResponse);

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"));
    }

    @Test
    void shouldGetUserProfileById() throws Exception {
        User user = User.builder()
                .username("testuser")
                .fullName("Test User")
                .role(Role.READER)
                .isActive(true)
                .build();
        user.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authService.getFollowerCount(1L)).thenReturn(10L);
        when(authService.getFollowingCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/auth/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.followerCount").value(10))
                .andExpect(jsonPath("$.followingCount").value(5));
    }

    @ParameterizedTest
    @CsvSource({
        "/follow/2, Followed successfully",
        "/unfollow/2, Unfollowed successfully",
        "/follow/2/approve, Follow request approved",
        "/follow/2/reject, Follow request rejected",
        "/deactivate/request, OTP sent to your email for account deactivation."
    })
    void shouldHandleAccountActions(String path, String expectedMessage) throws Exception {
        User user = User.builder().email("test@example.com").build();
        user.setUserId(1L);
        String token = "valid-token";

        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(authService.getUserByEmail("test@example.com")).thenReturn(user);

        mockMvc.perform(post("/auth" + path)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Test
    void shouldCheckIsFollowing() throws Exception {
        User user = User.builder().email("test@example.com").build();
        user.setUserId(1L);
        String token = "valid-token";

        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(authService.getUserByEmail("test@example.com")).thenReturn(user);
        when(authService.isFollowing(1L, 2L)).thenReturn(true);

        mockMvc.perform(get("/auth/is-following/2")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(true));
    }

    @Test
    void shouldUpdateProfile() throws Exception {
        User user = User.builder().email("test@example.com").build();
        user.setUserId(1L);
        String token = "valid-token";
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFullName("Updated Name");

        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(authService.getUserByEmail("test@example.com")).thenReturn(user);

        mockMvc.perform(put("/auth/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile updated successfully"));
    }

    @Test
    void shouldChangePassword() throws Exception {
        User user = User.builder().email("test@example.com").build();
        user.setUserId(1L);
        String token = "valid-token";
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword123!", "NewPassword123!");

        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(authService.getUserByEmail("test@example.com")).thenReturn(user);

        mockMvc.perform(put("/auth/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    void shouldSearchUsers() throws Exception {
        when(authService.searchUsers("test")).thenReturn(java.util.List.of(new User()));

        mockMvc.perform(get("/auth/search")
                .param("username", "test"))
                .andExpect(status().isOk());
    }


    @Test
    void shouldVerifyDeactivation() throws Exception {
        User user = User.builder().email("test@example.com").build();
        user.setUserId(1L);
        String token = "valid-token";

        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(authService.getUserByEmail("test@example.com")).thenReturn(user);

        mockMvc.perform(post("/auth/deactivate/verify")
                .header("Authorization", "Bearer " + token)
                .param("otp", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account deactivated and deleted successfully."));
    }

    @Test
    void shouldForgotPassword() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reset token sent to email"));
    }

    @Test
    void shouldResetPassword() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "token", "NewPassword123!");

        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }

    @Test
    void shouldBecomeAuthorRequest() throws Exception {
        OtpRequest request = new OtpRequest("test@example.com");

        mockMvc.perform(post("/auth/become-author/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent to your email for author verification."));
    }

    @Test
    void shouldBecomeAuthorVerify() throws Exception {
        VerifyRequest request = new VerifyRequest("test@example.com", "1234");

        mockMvc.perform(post("/auth/become-author/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Congratulations! You are now an Author."));
    }

    @Test
    void shouldGetFollowers() throws Exception {
        User user = User.builder().email("test@example.com").build();
        user.setUserId(1L);
        String token = "valid-token";
        User follower = User.builder().username("follower").role(Role.READER).isActive(true).build();
        follower.setUserId(2L);

        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(authService.getUserByEmail("test@example.com")).thenReturn(user);
        when(authService.getFollowers(1L)).thenReturn(java.util.List.of(follower));
        when(authService.getFollowStatus(2L, 1L)).thenReturn("APPROVED");

        mockMvc.perform(get("/auth/followers")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("follower"))
                .andExpect(jsonPath("$[0].followStatus").value("APPROVED"));
    }
}
